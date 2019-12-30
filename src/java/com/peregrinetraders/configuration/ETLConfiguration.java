package com.peregrinetraders.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class ETLConfiguration {


	private static Options getOptions() {
		Options options = new Options();
		options.addOption(new Option("c", "configuration", true, "Configuration file"));
		options.addOption(new Option("p", "prefix", true, "Prefix for the output files."));
		options.addOption(new Option("x", "exclude", true, "Message type code to exclude from processing. Option can be supplied multiple times."));
		options.addOption(new Option("o", "only", true, "Extract only this type of message." ));
		return options;
	}

	private static List<String> validateArguments(ETLConfigurationParameters params, HashSet<Integer> supportedMessageIds) {
		ArrayList<String> errors = new ArrayList<String>();
		
		if (params.hasPrefix()) {
			if (params.getPrefix().isEmpty()) {
				errors.add("Prefix must be a non-empty string.");
			}
		} else {
			errors.add("Prefix must be supplied.");
		}
		
		if (params.hasExclude() && params.hasOnly()) {
			errors.add("Cannot both exclude (-x) and only (-o) select a message type");
		} else {
			Consumer<Integer> numberCheck = p -> {
				if (!supportedMessageIds.contains(p)) {
					errors.add("Unknown message type code: " + p);
				}
			};

			if (params.hasExclude()) {
				for (Integer exclude : params.getExclude()) {
					numberCheck.accept(exclude);
				}
			}
			if (params.hasOnly()) {
				numberCheck.accept(params.getOnly());
			}
		}
		if (! params.hasFolders()) {
			errors.add("At least one file of folder must be supplied.");
		} else {
			for (String fileOrFolder : params.getFolders()) {
				File f = new File(fileOrFolder);
				if (!f.exists()) {
					errors.add("File or folder does not exist: " + fileOrFolder);
				}
			}
		}
		return errors;
	}
	
	private static ETLConfigurationParameters adaptFromCommandLine(String[] args) throws ParseException {
		ETLConfigurationParameters params = new ETLConfigurationParameters();
		CommandLineParser parser = new org.apache.commons.cli.PosixParser();
		CommandLine line = parser.parse(getOptions(), args);
		if (line.hasOption('c')) {
			params.setConfiguration(line.getOptionValue('c'));
		}
		if (line.hasOption('p')) {
			params.setPrefix(line.getOptionValue('p'));	
		}
		if (line.hasOption('x')) {
			try {
				String[] es = line.getOptionValues('x');
				Integer[] xs = new Integer[es.length];
				for (int i = 0; i < es.length; i++) {
					xs[i] = Integer.parseInt(es[i]);
				}
				params.setExclude(xs);
			} catch (NumberFormatException e) {
				throw new ParseException(e.getMessage());
			}
		}
		if (line.hasOption('o')) {
			params.setOnly(Integer.parseInt(line.getOptionValue('o')));
		}
		params.setFolders(line.getArgs());
		return params;
	}
	
	public static void printSummary(String programName) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(programName, getOptions());
	}
	public static ETLConfigurationParameters initializeAppConfiguration(
			String[] args,
			HashSet<Integer> supportedMessageIds) throws ETLConfigurationException {

		ETLConfigurationParameters cmdLine;
		try {
			cmdLine = adaptFromCommandLine(args);
		} catch (ParseException e) {
			throw new ETLConfigurationException("Cannot interpret command line parameters; errors: " + e.getMessage());
		}
		
		if (cmdLine.hasConfiguration()) {
			String configurationFileName = cmdLine.getConfiguration();
			File configurationFile = new File(configurationFileName);
			if (!configurationFile.exists()) {
				throw new ETLConfigurationException("Configuration file " + configurationFile + " not found.");
			} else {
				if (!configurationFile.isFile()) {
					throw new ETLConfigurationException("Configuration file " + configurationFile + " must be a file.");
				} else {
					try {
						cmdLine.fillInMissing(new ObjectMapper().readValue(configurationFile, ETLConfigurationParameters.class));
					} catch (JsonParseException e) {
						e.printStackTrace();
						throw new ETLConfigurationException("Error while parsing configuration file " + configurationFileName + ": " + e.getMessage());
					} catch (JsonMappingException e) {
						e.printStackTrace();
						throw new ETLConfigurationException("Error while reading configuration file" + configurationFileName + ": " + e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
						throw new ETLConfigurationException("Error encountered while trying to read configuration file" + configurationFileName + ":" + e.getMessage());
					}
				}
			}
		}
		List<String> possibleErrors = validateArguments(cmdLine, supportedMessageIds);
		if (!possibleErrors.isEmpty()) {
			throw new ETLConfigurationException(String.join("\n", possibleErrors));
		}
		return cmdLine;
	}

	public static HashSet<Integer> excludedMessages(
			ETLConfigurationParameters params,
			HashSet<Integer> supportedMessageIds
			) {
		if (params.hasOnly()) {
			HashSet<Integer> excludedMessages = new HashSet<Integer>(supportedMessageIds);
			excludedMessages.remove(params.getOnly());
			return excludedMessages;
		}
		if (params.hasExclude()) {
			return  new HashSet<Integer>(Arrays.asList(params.getExclude()));
		}
		return new HashSet<Integer>();
	}
	
	public static List<File> filesFromDir(String dir) throws IOException {
		return Files.walk(Paths.get(dir))
								.filter(Files::isRegularFile)
								.filter(p -> p.toString().endsWith(".gz"))
								.map(p -> p.toFile())
								.collect(Collectors.toList());
	}
}
