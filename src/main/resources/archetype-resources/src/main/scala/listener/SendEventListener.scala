package ${groupId}.${artifactId}.listener

import org.drools.KnowledgeBase
import org.drools.KnowledgeBaseFactory
import org.drools.builder.KnowledgeBuilder
import org.drools.builder.KnowledgeBuilderFactory
import org.drools.builder.ResourceType
import org.drools.io.ResourceFactory
import org.drools.runtime.StatelessKnowledgeSession
import org.drools.runtime.rule.ConsequenceException

import gov.nasa.jpf.Config
import gov.nasa.jpf.JPF
import gov.nasa.jpf.ListenerAdapter
import gov.nasa.jpf.jvm.JVM
import gov.nasa.jpf.jvm.MethodInfo
import gov.nasa.jpf.jvm.InfoObject
import gov.nasa.jpf.jvm.ElementInfo
import gov.nasa.jpf.jvm.bytecode.Instruction
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction

import java.io.ObjectInputStream
import java.io.FileInputStream
import java.io.ByteArrayInputStream

class ScalaMethodListener extends ListenerAdapter {
  private val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
  kbuilder.add(ResourceFactory.newClassPathResource("proeprties.drl"), ResourceType.DRL)

  if (kbuilder.hasErrors) {
    System.err.println(kbuilder.getErrors.toString)
  }

  private val kbase = KnowledgeBaseFactory.newKnowledgeBase
  kbase.addKnowledgePackages(kbuilder.getKnowledgePackages)

  private val ksession = kbase.newStatelessKnowledgeSession

  private def isSender(info: InfoObject): Boolean = {
    val annotationName = "com.cflewis.jpfools.annotation.SendEvent"

    return info.getAnnotations match {
      case null => false
      case ai => ai.exists(_.getName == annotationName)
    }
  }

  override def methodEntered(jvm: JVM) = {
    val mi = jvm.getLastMethodInfo

    if (isSender(mi) && jvm.getLastInstruction.isInstanceOf[InvokeInstruction]) {
      var call = jvm.getLastInstruction.asInstanceOf[InvokeInstruction]
      var args = call.getArgumentValues(jvm.getLastThreadInfo)
      for (arg <- args) {
        if (arg != null && arg.isInstanceOf[ElementInfo]) {
          val eiArg = arg.asInstanceOf[ElementInfo]
          val bin = new ByteArrayInputStream(eiArg.asByteArray)

          try {
            ksession.execute(new ObjectInputStream(bin).readObject)
          } catch {
            case e: ConsequenceException => 
                throw new Exception("Rule '" + e.getRule.getName + "' failed, "
                  + "due to " + e.getCause.toString)
          }
        }
      }
    }
  }
}