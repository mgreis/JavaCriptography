::certs
keytool -genkey -alias server -keyalg RSA -keypass keytool -storepass keytool -keystore server.jks
keytool -export -alias server -storepass keytool -file server.cer -keystore server.jks

keytool -genkey -alias joao -keyalg RSA -keypass keytool -storepass keytool -keystore joao.jks
keytool -export -alias joao -storepass keytool -file joao.cer -keystore joao.jks

keytool -genkey -alias ana -keyalg RSA -keypass keytool -storepass keytool -keystore ana.jks
keytool -export -alias ana -storepass keytool -file ana.cer -keystore ana.jks


::CA
keytool -genkey -noprompt -trustcacerts -keyalg RSA -alias ca -keypass keytool -keystore ca.jks -storepass keytool

::Verbose of *.jks
keytool -list -v  -keystore ca.jks -storepass keytool
keytool -list -v  -alias ca -keystore ca.jks -storepass keytool


::Sign certificates
::Server
keytool -import -v -trustcacerts -alias server -file server.cer -keystore servercerts.jks -keypass keytool -storepass keytool
keytool -import -v -trustcacerts -alias joao -file joao.cer -keystore servercerts.jks -keypass keytool -storepass keytool
keytool -import -v -trustcacerts -alias ana -file ana.cer -keystore servercerts.jks -keypass keytool -storepass keytool

::Joao
keytool -import -v -trustcacerts -alias joao -file joao.cer -keystore joaocerts.jks -keypass keytool -storepass keytool
keytool -import -v -trustcacerts -alias server -file server.cer -keystore joaocerts.jks -keypass keytool -storepass keytool

::Ana
keytool -import -v -trustcacerts -alias ana -file ana.cer -keystore anacerts.jks -keypass keytool -storepass keytool
keytool -import -v -trustcacerts -alias server -file server.cer -keystore anacerts.jks -keypass keytool -storepass keytool
