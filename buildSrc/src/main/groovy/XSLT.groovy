
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import org.gradle.api.tasks.SourceTask
import org.gradle.api.*;
import org.gradle.api.tasks.*;
import org.gradle.api.file.FileVisitDetails;


class XSLT extends SourceTask  {

 @OutputFile @Optional
 File destFile

 @OutputDirectory @Optional
 File destDir

 @InputFile
 File stylesheetFile

 @TaskAction
 def transform() {
   if (!((destFile != null) ^ (destDir != null))) {
     throw new InvalidUserDataException("Must specify output file or dir.")
   }

   def factory = TransformerFactory.newInstance()
   def transformer = factory.newTransformer(
                         new StreamSource(stylesheetFile))

   source.visit { FileVisitDetails fvd ->
     if (fvd.isDirectory()) {
       return
     }

     File d = destFile;
     if( d == null )
       d = new File( destDir, fvd.file.name )

     transformer.transform(new StreamSource(fvd.file),
                           new StreamResult(d))
   }
 }
}