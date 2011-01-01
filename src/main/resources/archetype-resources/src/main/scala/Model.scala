package ${groupId}.${artifactId}

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import ${groupId}.${artifactId}.annotation.SendEvent;

object ScalaRunner extends Application {

  @SendEvent
  def sendEvent(bytes: Array[Byte]) = {
    // Annotating a method that takes a byte array with SendEvent
    // will send the event for evaluation
  }

  def welcome(message: String) = {
    var bos = new ByteArrayOutputStream()
    var out = new ObjectOutputStream(bos)
    out.writeObject(message)
    out.close
    println(message)
    sendEvent(bos.toByteArray)
  }

  welcome("This is a " + ${artifactId} + " model.")
}
