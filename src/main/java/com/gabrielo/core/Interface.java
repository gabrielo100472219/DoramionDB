package com.gabrielo.core;

import java.util.Scanner;

public class Interface {

	private final Scanner scanner;
	private final SqlEngine engine;

	public Interface(SqlEngine engine) {
		this.scanner = new Scanner(System.in);
		this.engine = engine;
	}

	public Interface(Scanner scanner, SqlEngine engine) {
		this.scanner = scanner;
		this.engine = engine;
	}

	public void runDatabaseEngine() {
		printBanner();

		while (true) {
			System.out.print("> ");
			final String input = this.scanner.nextLine().trim();
			if (input.equals(".exit")) {
				break;
			}
			final SqlExecutionResult result = this.engine.executeStatement(input);
			System.out.println();
			System.out.println(result.message());
			System.out.println();
			if (result.queryResult().isEmpty()) {
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
