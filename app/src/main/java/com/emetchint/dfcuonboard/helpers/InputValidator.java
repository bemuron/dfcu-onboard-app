package com.emetchint.dfcuonboard.helpers;


import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class InputValidator {

  private static final int MIN_PASSWORD_LENGTH = 5;
  private static final int CODE_LENGTH = 10;

  /*
   * Validates input user phone number must be 10 characters
   * */
  public  boolean isPhoneNumberValid(String phone_number){
    //matches 10-digit numbers only
    String regexStr = "^[0-9]{10}$";
    return  phone_number.matches(regexStr);
  }

  /*
   * Validates input user NIN number must be 14 characters for UG nins
   * */
  public  boolean isNinValid(String user_nin){
    String regexStr = "^(CM|CF|PM|PF|RM|RF)[A-Za-z0-9]+$";
    return  user_nin.matches(regexStr);
  }


  /**
   * Validate hex with regular expression
   *
   * @param hex
   *            hex for validation
   * @return true valid hex, false invalid hex
   */
  public boolean isEmailValid(final String hex) {
    Pattern pattern;
    Matcher matcher;

    String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    pattern = Pattern.compile(EMAIL_PATTERN);

    matcher = pattern.matcher(hex);
    return matcher.matches();
  }

  /* Validates user password must be 4 or characters*/
  public boolean isPasswordValid(String password) {
    return password.length() >= MIN_PASSWORD_LENGTH;

  }

  //check for the code length. should be 10 digits
  public boolean isCodeValid(String code) {
    return code.length() == CODE_LENGTH;

  }
}
