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

import org.junit.jupiter.params.provider.ArgumentsSource;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(FolderArgumentsProvider.class)
public @interface FolderSource {
	
	String path() default ""; //root

	String pattern() default ""; //folder pattern
	
	FileMatchingMode mode() default SMART;
	
	Class<?> defaultType() default File.class; // FILE | PATH | URI | InputStream | String | String[] | byte[]
	
	enum FileMatchingMode {
		
		STRICT {
			@Override
			public File[] matchingFiles(String argName, File folder) {
				var p = compile("^" + argName + "(\\..+)?$", CASE_INSENSITIVE).asPredicate(); //filesys ignore case
				return folder.listFiles(f-> f.isFile() && p.test(f.getName()));
			}
		},
		
		SMART {
			@Override
			public File[] matchingFiles(String argName, File folder) {
				var name = argName.replace("_", "").toLowerCase();
				return folder.listFiles(f-> f.isFile() && name.contains(smartTransform(f.getName())));
			}
		};
		
		public abstract File[] matchingFiles(String argName, File folder);

		private static String smartTransform(String filename) {
			var idx = filename.lastIndexOf('.');
			if(idx > -1) {
				filename = filename.substring(0, idx);
			}
			return filename.replaceAll("[-_\\s\\.]", "").toLowerCase();
		}
	}
}
