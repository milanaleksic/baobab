swt-gui-transformer
===================

Dependencies
----------------------

SWT GUI Transformer depends on following frameworks/libraries:
 - (of course) SWT
 - Google Guava
 - Jackson
 - javax.inject provider (for example Google Guice or Spring)

If you don't use these libraries than you might need to fork the code
and try to hack your way through.

JRE required is 1.6.0


Messages string provider
----------------------
Use your DI container to override `net.milanaleksic.guitransformer.providers.ResourceBundleProvider`
with your implementation or just route it to SimpleResourceBundleProvider to always use messages_en.properties
(or default messages.properties if first does not exist).


Object provider
----------------------
When you use named object syntax to ask from the DI for a named object in JSON definition,
you basically ask it from implementation of `net.milanaleksic.guitransformer.providers.ObjectProvider`
you registered in the DI container.
You can though use AlwaysReturnNullObjectProvider to always embed null when named object is requested.


Some development notes:
----------------------

Since Eclipse doesn't really like to use Maven to store SWT artifacts (at least for now)
if you are not able to fetch Maven artifacts please install them locally manually.
I know it's not a popular approach but there's nothing I can do :)

mvn install:install-file -DgroupId=org.eclipse.swt
    -DartifactId=org.eclipse.swt.win32.win32.x86 -Dversion=3.7.2
    -Dfile=swt-3.7.2-win32-win32-x86-debug.jar -Dpackaging=jar -DgeneratePom=true

I propose using debug artifacts since it allows you to do so much more in debug mode
in you IDE than normal artifacts! Of course in production mode do pack the non-debug version
besides the jar of GUI Transformer. This is the approach used in Eclipse's testing Maven
repository used in the POM of the project - although it is not up to date with latest
stable SWT version - not by a long shot.