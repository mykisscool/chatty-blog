name := "ChattyBlog"
 
version := "1.0" 
      
lazy val `chattyblog` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( 
  jdbc , 
  ehcache , 
  ws , 
  specs2 % Test , 
  guice ,
  evolutions ,
  "mysql" % "mysql-connector-java" % "5.1.29" ,
  "org.mindrot" % "jbcrypt" % "0.3m" ,
  "org.playframework.anorm" %% "anorm" % "2.6.2"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )