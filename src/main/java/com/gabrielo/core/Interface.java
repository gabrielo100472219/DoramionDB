package com.gabrielo.core;

import java.util.Scanner;

public class Interface {

  private final Scanner scanner;
  private final SqlEngine engine;
  private final Runnable onExit;

  public Interface(SqlEngine engine) {
    this(new Scanner(System.in), engine, () -> {});
  }

  public Interface(SqlEngine engine, Runnable onExit) {
    this(new Scanner(System.in), engine, onExit);
  }

  public Interface(Scanner scanner, SqlEngine engine) {
    this(scanner, engine, () -> {});
  }

  public Interface(Scanner scanner, SqlEngine engine, Runnable onExit) {
    this.scanner = scanner;
    this.engine = engine;
    this.onExit = onExit;
  }

  public void runDatabaseEngine() {
    printBanner();

    while (true) {
      System.out.print("> ");
      final String input = this.scanner.nextLine().trim();
      if (input.equals(".exit")) {
        onExit.run();
        break;
      }
      final SqlExecutionResult result = this.engine.executeStatement(input);
      System.out.println();
      System.out.println(result.message());
      System.out.println();
      if (!result.queryResult().isEmpty()) {
        result.queryResult().stream().forEach(System.out::println);
      }
      System.out.println();
    }
  }

  private static void printBanner() {
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println(
        "████████▄   ▄██████▄     ▄████████    ▄████████   ▄▄▄▄███▄▄▄▄    ▄█   ▄██████▄  ███▄▄▄▄   ████████▄  ▀█████████▄  \n"
            + "███   ▀███ ███    ███   ███    ███   ███    ███ ▄██▀▀▀███▀▀▀██▄ ███  ███    ███ ███▀▀▀██▄ ███   ▀███   ███    ███ \n"
            + "███    ███ ███    ███   ███    ███   ███    ███ ███   ███   ███ ███▌ ███    ███ ███   ███ ███    ███   ███    ███ \n"
            + "███    ███ ███    ███  ▄███▄▄▄▄██▀   ███    ███ ███   ███   ███ ███▌ ███    ███ ███   ███ ███    ███  ▄███▄▄▄██▀  \n"
            + "███    ███ ███    ███ ▀▀███▀▀▀▀▀   ▀███████████ ███   ███   ███ ███▌ ███    ███ ███   ███ ███    ███ ▀▀███▀▀▀██▄  \n"
            + "███    ███ ███    ███ ▀███████████   ███    ███ ███   ███   ███ ███  ███    ███ ███   ███ ███    ███   ███    ██▄ \n"
            + "███   ▄███ ███    ███   ███    ███   ███    ███ ███   ███   ███ ███  ███    ███ ███   ███ ███   ▄███   ███    ███ \n"
            + "████████▀   ▀██████▀    ███    ███   ███    █▀   ▀█   ███   █▀  █▀    ▀██████▀   ▀█   █▀  ████████▀  ▄█████████▀  \n"
            + "                        ███    ███                                                                                ");
    System.out.println();
    System.out.println();
    System.out.println();
  }
}
