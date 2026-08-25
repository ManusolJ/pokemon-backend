package com.poketeambuilder.configuration;

import com.poketeambuilder.controllers.AdminController;
import com.poketeambuilder.controllers.PokemonController;
import com.poketeambuilder.controllers.TeamController;
import com.poketeambuilder.controllers.UserController;

import com.poketeambuilder.infrastructure.security.AuthEntryPoint;
import com.poketeambuilder.infrastructure.security.AuthAccessDeniedHandler;
import com.poketeambuilder.infrastructure.security.AuthRateLimitFilter;
import com.poketeambuilder.infrastructure.security.JwtAuthenticationFilter;
import com.poketeambuilder.infrastructure.security.SecurityErrorWriter;

import com.poketeambuilder.services.auth.JwtService;
import com.poketeambuilder.services.command.SeedLogCommandService;
import com.poketeambuilder.services.command.TeamCommandService;
import com.poketeambuilder.services.command.UserCommandService;
import com.poketeambuilder.services.query.AuditLogQueryService;
import com.poketeambuilder.services.query.PokemonQueryService;
import com.poketeambuilder.services.query.SeedLogQueryService;
import com.poketeambuilder.services.query.TeamQueryService;
import com.poketeambuilder.services.query.UserQueryService;

import com.poketeambuilder.entities.SeedLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Exercises {@code SecurityConfig} against the real controller routes.
 *
 */
@WebMvcTest(controllers = {
    TeamController.class,
    UserController.class,
    AdminController.class,
    PokemonController.class,
})
@Import({
    JwtService.class,
    CacheConfig.class,
    SecurityConfig.class,
    AuthEntryPoint.class,
    AuthRateLimitFilter.class,
    SecurityErrorWriter.class,
    AuthAccessDeniedHandler.class,
    JwtAuthenticationFilter.class,
})
@ActiveProfiles("test")
@EnableConfigurationProperties({ CorsProperties.class, JwtProperties.class })
class SecurityRulesTest {

    private static final String EMPTY_FILTER = "{}";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PokemonQueryService pokemonQueryService;
    @MockitoBean private TeamQueryService teamQueryService;
    @MockitoBean private TeamCommandService teamCommandService;
    @MockitoBean private UserQueryService userQueryService;
    @MockitoBean private UserCommandService userCommandService;
    @MockitoBean private SeedLogQueryService seedLogQueryService;
    @MockitoBean private SeedLogCommandService seedLogCommandService;
    @MockitoBean private AuditLogQueryService auditLogQueryService;

    @BeforeEach
    void stubSeedTrigger() {
        when(seedLogCommandService.triggerSeed(any())).thenReturn(SeedLog.builder().id(1L).build());
    }

    @Nested
    @DisplayName("Public catalogue")
    class PublicCatalogue {

        @Test
        @DisplayName("The four read paths are reachable without a token")
        void catalogueReadsAreOpen() throws Exception {
            for (String path : new String[] { "filter", "id", "summaries", "count" }) {
                mockMvc.perform(post("/api/pokemon/" + path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(EMPTY_FILTER))
                        .andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("A path under the same prefix that isn't listed is not open")
        void unlistedPathsUnderTheSamePrefixAreClosed() throws Exception {
            mockMvc.perform(post("/api/pokemon/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(EMPTY_FILTER))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Public team reads are open; the rest of /api/teams is not")
        void onlyPublicTeamReadsAreOpen() throws Exception {
            mockMvc.perform(post("/api/teams/public/filter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(EMPTY_FILTER))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/teams/me/filter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(EMPTY_FILTER))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authenticated routes")
    class AuthenticatedRoutes {

        @Test
        @DisplayName("Creating a team requires a token")
        void teamCreationRequiresAuthentication() throws Exception {
            mockMvc.perform(post("/api/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(EMPTY_FILTER))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("The profile route requires a token")
        void profileRequiresAuthentication() throws Exception {
            mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/users/me").with(user("ash").roles("USER")))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Administration")
    class Administration {

        @Test
        @DisplayName("Anonymous callers get 401 from the admin routes")
        void adminRoutesRejectAnonymous() throws Exception {
            mockMvc.perform(post("/api/admin/seed")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("A signed-in non-admin gets 403, not 401")
        void adminRoutesRejectOrdinaryUsers() throws Exception {
            mockMvc.perform(post("/api/admin/seed").with(user("ash").roles("USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("An admin reaches the seed trigger")
        void adminReachesTheSeedTrigger() throws Exception {
            mockMvc.perform(post("/api/admin/seed").with(user("oak").roles("ADMIN")))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("The nested admin routes on other resources are guarded too")
        void nestedAdminRoutesAreGuarded() throws Exception {
            mockMvc.perform(post("/api/users/admin/filter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(EMPTY_FILTER)
                            .with(user("ash").roles("USER")))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/teams/admin/1").with(user("ash").roles("USER")))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/teams/admin/1").with(user("oak").roles("ADMIN")))
                    .andExpect(status().isNoContent());
        }
    }
}
