name := """play-sandbox"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.6",
  "com.typesafe.akka" % "akka-camel_2.11" % "2.3.6",
  "org.scala-lang" % "scala-library" % "2.10.4",
  "org.apache.camel" % "camel-amqp" % "2.14.0",
  "org.apache.camel" % "camel-jaxb" % "2.14.0",
  "org.slf4j" % "slf4j-simple" % "1.6.6",
  "org.apache.qpid" % "qpid-client" % "0.30",
  "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "1.0"
)
