package edu.sc.seis.sod;

import java.lang.reflect.Method;

import org.apache.log4j.BasicConfigurator;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class PreStart {

	public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
		if (System.getProperty("java.vm.name").equals("GNU libgcj")) {
			System.err
					.println("You are running GNU's version of Java, gcj, which doesn't have all the features SOD requires.  Instead, use Sun's Java from http://java.sun.com.");
			System.exit(-1);
		}
		try {
			Class realStart = Class.forName(args[0]);
			String[] realArgs = new String[args.length - 1];
			System.arraycopy(args, 1, realArgs, 0, realArgs.length);
			Method main = realStart.getMethod("main",
					new Class[] { String[].class });
			main.invoke(null, new Object[] { realArgs });
		} catch (Throwable t) {
			GlobalExceptionHandler.handle(t);
		}
	}
}
