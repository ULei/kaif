package io.kaif.test;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.kaif.config.SpringProfile;
import io.kaif.config.UtilConfiguration;
import io.kaif.config.WebConfiguration;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.service.AccountService;
import io.kaif.service.ArticleService;
import io.kaif.service.ZoneService;

@ActiveProfiles(SpringProfile.TEST)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = MvcIntegrationTests.WebTestApplication.class)
public abstract class MvcIntegrationTests implements ModelFixture {

  @Profile(SpringProfile.TEST)
  @EnableWebMvc
  @ComponentScan(basePackages = "io.kaif.web")
  @Import(value = { UtilConfiguration.class, MockTestConfig.class, WebConfiguration.class,
      FreeMarkerAutoConfiguration.class })
  public static class WebTestApplication {
  }

  @Profile(SpringProfile.TEST)
  @Configuration
  static class MockTestConfig {
    @Bean
    public ArticleService articleService() {
      return Mockito.mock(ArticleService.class);
    }

    @Bean
    public AccountService accountService() {
      return Mockito.mock(AccountService.class);
    }

    @Bean
    public ZoneService zoneService() {
      return Mockito.mock(ZoneService.class);
    }
  }

  @Autowired
  protected AccountService accountService;

  @Autowired
  protected ZoneService zoneService;

  @Autowired
  protected ArticleService articleService;

  @Autowired
  private WebApplicationContext wac;

  protected MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    Mockito.reset(accountService, zoneService, articleService);
  }

  protected final String prepareAccessToken(Account account) {
    String token = account.getUsername() + "-token";
    AccountAccessToken accountAccessToken = new AccountAccessToken(account.getAccountId(),
        "pw",
        account.getAuthorities());
    when(accountService.tryDecodeAccessToken(token)).thenReturn(Optional.of(accountAccessToken));
    return token;
  }
}