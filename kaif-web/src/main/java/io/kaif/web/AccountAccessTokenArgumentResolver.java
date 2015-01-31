package io.kaif.web;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.kaif.service.AccountService;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.web.support.AccessDeniedException;

public class AccountAccessTokenArgumentResolver implements HandlerMethodArgumentResolver {

  private final AccountService accountService;

  public AccountAccessTokenArgumentResolver(AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == AccountAccessToken.class;
  }

  //TODO unit test
  @Override
  public AccountAccessToken resolveArgument(MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    String token = webRequest.getHeader("X-KAIF-ACCESS-TOKEN");
    HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
    Optional<AccountAccessToken> verified;

    //GET operation are calculated in memory only
    if (nativeRequest.getMethod().equalsIgnoreCase("GET")) {
      verified = accountService.tryDecodeAccessToken(token);
    } else {
      //for other mutation operation, always check in db
      verified = accountService.verifyAccessToken(token);
    }
    return verified.orElseThrow(AccessDeniedException::new);
  }
}
