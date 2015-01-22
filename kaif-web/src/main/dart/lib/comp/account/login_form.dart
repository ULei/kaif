library login_form;

import 'dart:html';
import 'package:kaif_web/service/service.dart';
import 'package:kaif_web/model.dart';

class LoginForm {
  final Element elem;
  final AccountService accountService;
  final AccountDao accountDao;
  LoginForm(this.elem, this.accountService, this.accountDao) {
    elem.onSubmit.listen(_login);
  }

  void _login(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement nameInput = elem.querySelector('#nameInput');
    TextInputElement passwordInput = elem.querySelector('#passwordInput');
    CheckboxInputElement rememberMeInput = elem.querySelector('#rememberMeInput');

    //TODO validate input
    //TODO show loading
    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    accountService.authenticate(nameInput.value, passwordInput.value)//
    .then((AccountAuth accountAuth) {
      accountDao.saveAccount(accountAuth, rememberMe:rememberMeInput.checked);
      //TODO handle ?from=
      window.location.href = '/'  ;
    }).whenComplete(() => submit.disabled = false);

  }

}