/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.javaagent.tracing;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaAgentTracing implements ClassFileTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaAgentTracing.class);
  private static final String LOGGING_CODE_START = "org.slf4j.LoggerFactory.getLogger(\"trace\").info(\"%s %s.%s\");";
  private static final String LOGGING_CODE_END = "org.slf4j.LoggerFactory.getLogger(\"trace\").info(\"%s %s.%s,{}ms\", %s + \"\");";
  private static final String APPLICATION_PACKAGE = "io/neocdtv";
  private static final String START = "CALL-START";
  private static final String END = "CALL-END";

  public static void premain(String agentArgument,
          Instrumentation instrumentation) {
    LOGGER.info("premain");
    instrumentation.addTransformer(new JavaAgentTracing());
  }

  @Override
  public byte[] transform(ClassLoader loader, String className,
          Class clazz, java.security.ProtectionDomain domain,
          byte[] bytes) {
    if (className.startsWith(APPLICATION_PACKAGE)) {
      LOGGER.info("working on class " + className);
      return doClass(className, clazz, bytes);
    }
    return bytes;
  }

  private byte[] doClass(String name, Class clazz, byte[] b) {
    ClassPool pool = ClassPool.getDefault();

    CtClass cl = null;
    try {
      cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
      if (!cl.isInterface()) {

        CtMethod[] methods = cl.getMethods();

        for (int i = 0; i < methods.length; i++) {
          final CtMethod method = methods[i];
          if (!method.isEmpty() && !isNative(method)) {
            LOGGER.info("working on method " + method.getName());
            doMethod(method, name);
          }
        }
        b = cl.toBytecode();
      }
    } catch (IOException | RuntimeException | CannotCompileException | NotFoundException e) {
      LOGGER.error(e.getMessage(), e);
    } finally {
      if (cl != null) {
        cl.detach();
      }
    }
    return b;
  }

  private void doMethod(CtBehavior method, String className)
          throws NotFoundException, CannotCompileException {
    final String classNameDotted = className.replace('/', '.');
    final String metdhoName = method.getMethodInfo().getName();

    final String callStart = String.format(LOGGING_CODE_START, START,
            classNameDotted,
            metdhoName);
    LOGGER.info("Inserting before method: {}" + callStart);
    method.insertBefore(callStart);
    
    method.addLocalVariable("startMs", CtClass.longType);
    method.insertBefore("startMs = System.currentTimeMillis();");
    
    final String callEnd = String.format(LOGGING_CODE_END, END,
            classNameDotted,
            metdhoName, "System.currentTimeMillis() - startMs");
    LOGGER.info("Inserting after method: {}" + callEnd);
    method.insertAfter(callEnd);
  }

  public static boolean isNative(CtMethod method) {
    return Modifier.isNative(method.getModifiers());
  }

}
