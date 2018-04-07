package comp250;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.Policy;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import tournaments.LoadTournamentAIs;

public class ListTournamentAIsInJar {
	
	public static void main(String[] args) throws IOException, ReflectiveOperationException {
		
		Policy.setPolicy(new SandboxSecurityPolicy());
		System.setSecurityManager(new SecurityManager());

		String jarPath = args[0];
		//String jarPath = "../bot/bot.jar";

		ClassLoader loader = new PluginClassLoader(new File(jarPath).toURI().toURL());

		try {
			Class<?> overrideClass = loader.loadClass("comp250.OverrideTournamentAIs");
			Method method = overrideClass.getMethod("getClassNames");
			String[] names = (String[]) method.invoke(null);
			for (String name : names) {
				System.out.println(name);
			}
			return;
		} catch (ClassNotFoundException e) { // ignore the exception if OverrideTournamentAIs doesn't exist
		}
		
		URL jar = new File(jarPath).toURI().toURL();
		ZipInputStream zip = new ZipInputStream(jar.openStream());
		while (true) {
			ZipEntry e = zip.getNextEntry();
			if (e == null)
				break;
			String name = e.getName();
			if (name.endsWith(".class")) {
				String className = name.substring(0, name.length() - 6).replace('/', '.');
				Class<?> c = loader.loadClass(className);
				if (!Modifier.isAbstract(c.getModifiers()) && AI.class.isAssignableFrom(c)) {
					System.out.println(className);
				}
			}
		}
	}
}
