package net.milanaleksic.guitransformer.util;

import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.*;

import static net.milanaleksic.guitransformer.util.ObjectUtil.defineClass;
import static org.objectweb.asm.Opcodes.*;

/**
 * User: Milan Aleksic
 * Date: 2/19/13
 * Time: 8:31 AM
 * <p/>
 * Methods you want wrapped must be:
 * <ol>
 * <li>of void return type</li>
 * </ol>
 */
public class ProxyFactoryForPostProcessingOfObservableMethods {

    private static final Map<String, Class<?>> cachedProxyClassMap = new HashMap<>();

    public interface MethodPostProcessor<T> {
        public void postProcess(T target);
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrapMethodCalls(Class<T> modelClass, Set<Method> observableMethods,
                                        MethodPostProcessor methodPostProcessor) {
        final Type modelType = Type.getType(modelClass);
        final Type proxyType = Type.getType("L" + modelType.getInternalName() + "PostProcessingProxy;");
        try {
            final Class<?> classDefinition;
            synchronized (cachedProxyClassMap) {
                if (cachedProxyClassMap.containsKey(proxyType.getClassName()))
                    classDefinition = cachedProxyClassMap.get(proxyType.getClassName());
                else {
                    ClassWriter cw = getClassWriter(observableMethods, modelType, proxyType);
                    classDefinition = defineClass(proxyType.getClassName(), cw.toByteArray());
                    cachedProxyClassMap.put(proxyType.getClassName(), classDefinition);
                }
            }
            return (T) classDefinition.getConstructors()[0].newInstance(methodPostProcessor);
        } catch (Exception ex) {
            throw new RuntimeException("Error constructing constructor access class: " + proxyType.getClassName(), ex);
        }
    }

    private static ClassWriter getClassWriter(Set<Method> observableMethods, Type modelType, Type proxyType) {
        final Type thisType = Type.getType(ProxyFactoryForPostProcessingOfObservableMethods.class);
        final Type postProcessorType = Type.getType(MethodPostProcessor.class);
        final ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, proxyType.getInternalName(), null, modelType.getInternalName(), null);
        cw.visitInnerClass(postProcessorType.getInternalName(), thisType.getInternalName(),
                "MethodPostProcessor", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        createPostProcessorField(modelType, postProcessorType, cw);
        createConstructor(modelType, proxyType, postProcessorType, cw);
        for (Method method : observableMethods)
            createOverriddenMethod(modelType, proxyType, postProcessorType, cw, method);

        cw.visitEnd();
        return cw;
    }

    private static void createPostProcessorField(Type modelType, Type postProcessorType, ClassWriter cw) {
        FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "postProcessor", postProcessorType.getDescriptor(),
                "L" + postProcessorType.getInternalName() + "<" + modelType.getDescriptor() + ">;", null);
        fv.visitEnd();
    }

    private static void createOverriddenMethod(Type modelType, Type proxyType, Type postProcessorType, ClassWriter cw, Method method) {
        MethodVisitor mv;
        int noOfParams = 1;
        mv = cw.visitMethod(0, method.getName(), Type.getMethodDescriptor(method), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        for (Class<?> parameterType : method.getParameterTypes()) {
            final int opCode = Type.getType(parameterType).getOpcode(ILOAD);
            mv.visitVarInsn(opCode, noOfParams++);
            if (opCode == LLOAD || opCode == DLOAD)
                noOfParams++;
        }
        mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), method.getName(), Type.getMethodDescriptor(method));
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, proxyType.getInternalName(), "postProcessor", postProcessorType.getDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEINTERFACE, postProcessorType.getInternalName(), "postProcess", "(Ljava/lang/Object;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1 + noOfParams, 1 + noOfParams);
        mv.visitEnd();
    }

    private static void createConstructor(Type modelType, Type proxyType, Type postProcessorType, ClassWriter cw) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + postProcessorType.getDescriptor() + ")V",
                "(L" + postProcessorType.getInternalName() + "<" + modelType.getDescriptor() + ">;)V", null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), "<init>", "()V");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, proxyType.getInternalName(), "postProcessor", postProcessorType.getDescriptor());
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

}
