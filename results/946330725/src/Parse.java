import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parse {
  public static final boolean DEBUG = false;
  public static void main(String [] args) throws Exception {
    /** This is the main entrance of the parser **/
    // read the first input token
    
    // loop variable
    int i;
    // holds true until there is nothing to read
    String curr_token = "";
    List<String> tokens = new ArrayList<String>();
    // CHANGE fr.read() TO br.read() WHEN SUBMITTING
    if (DEBUG) {
      FileReader fr = new FileReader("testcases\\hw1\\tobytest");
      while ((i = fr.read()) != -1) {
        curr_token = read_token((char)i, curr_token, tokens);
      }
      fr.close();
    } else {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      while ((i = br.read()) != -1) {
        curr_token = read_token((char)i, curr_token, tokens);
      }
      br.close();
    }

    // after the completion of the while loop all the tokens are in the tokens list
    if (match(tokens)) {
      System.out.println("Program parsed successfully");
    } else {
      System.out.print("Parse error\n");
    }
  }

  public static boolean match(List<String> tokens) {
    // make sure that expected is a valid token or keyword
    // throw error if not a match
    tokens = SL(tokens);
    return tokens.size() == 0;
  }


  //  ==================== all of the non-terminal methods ============================

  public static List<String> SL(List<String> tokens) {
    if (DEBUG) {
      System.out.print("SL: ");
      System.out.println(tokens);
    }
    // LOGIC: If empty string aka no tokens left, return, otherwise do S ; SL
    // LOGIC = FLAWED
    // If empty string we end, but also if the next token is }
    // if there is another token that can follow SL we are f'ed
    if (tokens.size() != 0 && !tokens.get(0).equals("}")) {
      if (tokens.get(0).equals("0error")) {
        return tokens;
      }
      tokens = S(tokens);
      tokens = eat(tokens, ";");
      tokens = SL(tokens);
      // how can I escape the loop?
    } 
    return tokens;
  }

  public static List<String> S(List<String> tokens) {
    if (DEBUG) {
      System.out.print("S: ");
      System.out.println(tokens);
    }
    // Next token should be: if, while, or an identifier, or print
    if (tokens.get(0).equals("if")) {
      tokens = eat(tokens, "if");
      tokens = eat(tokens, "(");
      tokens = C(tokens);
      tokens = eat(tokens, ")");
      tokens = eat(tokens, "{");
      tokens = SL(tokens);
      tokens = eat(tokens, "}");
      tokens = eat(tokens, "else");
      tokens = eat(tokens, "{");
      tokens = SL(tokens);
      tokens = eat(tokens, "}");
    } else if (tokens.get(0).equals("while")) {
      tokens = eat(tokens, "while");
      tokens = eat(tokens, "(");
      tokens = C(tokens);
      tokens = eat(tokens, ")");
      tokens = eat(tokens, "{");
      tokens = SL(tokens);
      tokens = eat(tokens, "}");
    } else if (tokens.get(0).equals("print")) {
      tokens = eat(tokens, "print");
      tokens = eat(tokens, "(");
      tokens = E(tokens);
      tokens = eat(tokens, ")");
    } else {
      // Must be an identifier, or its wrong
      if (test_id(tokens.get(0))) {
        tokens.remove(0);
        tokens = eat(tokens, ":=");
        tokens = E(tokens);
      } else {
        // error 
        return error(tokens);

      }
    }
    return tokens;
  }

  public static List<String> C(List<String> tokens) {
    if (DEBUG) {
      System.out.print("C: ");
      System.out.println(tokens);
    }
    // true or false or ERE
    if (tokens.get(0).equals("true")) {
      tokens = eat(tokens, "true");
    } else if (tokens.get(0).equals("false")) {
      tokens = eat(tokens, "false");
    } else {
      tokens = E(tokens);
      tokens = R(tokens);
      tokens = E(tokens);
    }
    return tokens;
  }

  public static List<String> E(List<String> tokens) {
    if (DEBUG) {
      System.out.print("E: ");
      System.out.println(tokens);
    }
    tokens = T(tokens);
    tokens = TT(tokens);
    return tokens;
  }

  public static List<String> TT(List<String> tokens) {
    if (DEBUG) {
      System.out.print("TT: ");
      System.out.println(tokens);
    }
    // B must be either + or -, so check if first token is + or -, then assume empty string if not (else branch ommitted because of code factoring)
    if (tokens.get(0).equals("+")||tokens.get(0).equals("-")) {
      tokens = B(tokens);
      tokens = T(tokens);
      tokens = TT(tokens);
    } 
    return tokens;
  }

  public static List<String> T(List<String> tokens) {
    if (DEBUG) {
      System.out.print("T: ");
      System.out.println(tokens);
    }
    // (E) or identifier or read identifier or num
    if (tokens.get(0).equals("(")) {
      tokens = eat(tokens, "(");
      tokens = E(tokens);
      tokens = eat(tokens, ")");
    } else if (tokens.get(0).equals("read")) {
      // read id
      tokens = eat(tokens, "read");
      if (test_id(tokens.get(0))) {
        tokens.remove(0);
      } else {
        // error
        return error(tokens);

      }
    } else if (test_id(tokens.get(0)) || test_num(tokens.get(0))) {
      // if identifier or number we do the same thing
      tokens.remove(0);
    } else {
      // error
      return error(tokens);
    }
    return tokens;
  }

  public static List<String> R(List<String> tokens) {
    if (DEBUG) {
      System.out.print("R: ");
      System.out.println(tokens);
    }
    // = or < or >
    if (tokens.get(0).equals("=") || tokens.get(0).equals("<") || tokens.get(0).equals(">")) {
      tokens.remove(0);
    } else {
      // error
      return error(tokens);

    }
    return tokens;
  }

  public static List<String> B(List<String> tokens) {
    if (DEBUG) {
      System.out.print("B: ");
      System.out.println(tokens);
    }
    // + or -
    if (tokens.get(0).equals("+") || tokens.get(0).equals("-")) {
      tokens.remove(0);
    } else {
      // error
      return error(tokens);
    }
    return tokens;
  }

  // =============== HELPER FUNCTIONS =================

  // Takes in tokens list and provided token and checks to make sure they match. Then it removes the token from the list
  public static List<String> eat(List<String> tokens, String token) {
    if (tokens.get(0).equals(token)) {
      tokens.remove(0);
      return tokens;
    } else {
      // Parse error
      return error(tokens);
    }
  }

  public static boolean test_num(String token) {
    // given a string test if it can be represented as a number
    for (int i = 0; i < token.length(); i++) {
      if (!Character.isDigit(token.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean test_id(String token) {
    // given a string test if it can be an identifier
    // Make sure the first character in the string is a letter, then the rest can be letters or digits

    // Make sure the identifier is not a keyword before proceding
    if (token.equals("true") || token.equals("false") || token.equals("if") || token.equals("while") || 
        token.equals("else") || token.equals("print") || token.equals("read")) {
      return false;
    }

    

    if (Character.isLetter(token.charAt(0))) {
      for (int i = 1; i < token.length(); i++) {
        if (!(Character.isDigit(token.charAt(i)) || Character.isLetter(token.charAt(i)))) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public static List<String> error(List<String> tokens) {
    for (int i = 0; i < tokens.size(); i++) {
      tokens.set(i, "0error");
    }
    return tokens;
  }


  // ================ TOKENIZER FUNCTION =================

  // Takes in the current character, the current token, and the list of tokens
  // Returns the current token and adds the current token to the list of tokens
  public static String read_token(char c, String curr_token, List<String> tokens) {
    boolean is_operator = (c == ')' || c == '{' || c == '}' ||
                           c == '+' || c == '-' || c == '=' ||
                           c == '>' || c == '<' || c == ';' || c == '(');
    boolean is_whitespace = (c == ' ' || c == '\n'|| c == '\r' || c == '\t');

    if (is_whitespace) {
      if (curr_token.length() > 0) {
        tokens.add(curr_token); // add the current token to the list
        curr_token = "";        // reset current token for next token
      }      // Make the next token as long as our current token is not empty (we don't need tokens with nothing in them)
    } else if (is_operator) {
      if (c == '=' && curr_token.equals(":")) {
        // special case of :=
        curr_token += c;
        tokens.add(curr_token);
        curr_token = "";
      } else {
        // check if there is a current token before or just whitespace
        if (curr_token.length() > 0) {
          tokens.add(curr_token);
        }  
        tokens.add(c + "");    // Since operators are just one character we add immediately
        curr_token = "";       // reset current token for next token

      }
    } else if (c == ':' && curr_token.length() > 0) {
      // Special case that there is an id with no space after it but a := after it
      tokens.add(curr_token); // add the current token to the list (it should be an identifier)
      curr_token = c + ""; // makes current token ":" so that next iteration of the loop will add the "=" to it
    } else {
      // This is a variable name or maybe a number or a keyword, so we just add to the current token
      curr_token += c;
    }
    return curr_token;
  }
}