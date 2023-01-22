package org.usf.junit.addons;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.usf.junit.addons.FolderSource.FileMatchingMode.SMART;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

import org.junit.jupiter.params.provider.ArgumentsSource;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(FolderArgumentsProvider.class)
public @interface FolderSource {
	
	String path() default ""; //root

	String pattern() default ""; //regex
	
	FileMatchingMode mode() default SMART;
	
	Class<?> defaultType() default File.class; // FILE | URI | PATH | InputStream | String
	
	enum FileMatchingMode {
		
		STRICT {
			@Override
			public File[] matchingFiles(Parameter arg, File folder) {
				var p = compile(arg.getName() + "(\\..+)?$", CASE_INSENSITIVE).asPredicate(); //filesys ignore case
				return folder.listFiles(f-> f.isFile() && p.test(f.getName()));
			}
		},
		
		SMART {
			@Override
			public File[] matchingFiles(Parameter arg, File folder) {
				var argName = arg.getName().replace("_", "").toLowerCase();
				return folder.listFiles(f-> f.isFile() && argName.contains(smartTransforme(f.getName())));
			}
		};
		
		public abstract File[] matchingFiles(Parameter arg, File folder);

		private static String smartTransforme(String filename) {
			var idx = filename.lastIndexOf('.');
			if(idx > -1) {
				filename = filename.substring(0, idx);
			}
			return filename.replaceAll("[-_\\s\\.]", "").toLowerCase();
		}
	}
	
}
