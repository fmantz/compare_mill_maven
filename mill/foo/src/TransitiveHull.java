
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassQueue;
import org.apache.bcel.util.ClassSet;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Taken from https://github.com/apache/commons-bcel/blob/master/src/examples/TransitiveHull.java and modified
 */
public class TransitiveHull extends EmptyVisitor {

    public static final String[] IGNORED = {"java[.].*", "javax[.].*", "sun[.].*", "sunw[.].*", "com[.]sun[.].*", "org[.]omg[.].*", "org[.]w3c[.].*",
            "org[.]xml[.].*", "net[.]jini[.].*"};

    public static String getHull(String argument) {
        JavaClass javaClass;
        try {
            if ((javaClass = Repository.lookupClass(argument)) == null) {
                javaClass = new ClassParser(argument).parse();
            }

            final TransitiveHull hull = new TransitiveHull(javaClass);

            hull.start();
            return Arrays.asList(hull.getClassNames()).toString();
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final ClassQueue queue;
    private final ClassSet set;

    private ConstantPool cp;

    private String[] ignored = IGNORED;

    public TransitiveHull(final JavaClass clazz) {
        queue = new ClassQueue();
        queue.enqueue(clazz);
        set = new ClassSet();
        set.add(clazz);
    }

    private void add(String className) {
        className = Utility.pathToPackage(className);

        for (final String alreadyContained : set.getClassNames()) {
            if (alreadyContained.equals(className)) {
                return;
            }
        }

        for (final String anIgnored : ignored) {
            if (Pattern.matches(anIgnored, className)) {
                return;
            }
        }

        try {
            final JavaClass clazz = Repository.lookupClass(className);
            if (set.add(clazz)) {
                queue.enqueue(clazz);
            }
            for(Field f:clazz.getFields()){
                final String signature = f.getSignature();
                checkType(Type.getType(signature));
            }
            for(Method m:clazz.getMethods()){
                final String signature = m.getSignature();
                final Type type = Type.getReturnType(signature);
                checkType(type);
                for (final Type type1 : Type.getArgumentTypes(signature)) {
                    checkType(type1);
                }


                // TEST
                LocalVariableTable lvt = m.getLocalVariableTable();
                if(lvt != null) {
                    LocalVariable[] lv = lvt.getLocalVariableTable();
                    for (LocalVariable l : lv) {
                        final String localSignature = l.getSignature();
                        System.out.println("FIND G>" + Type.getType(localSignature));
                        checkType(Type.getType(localSignature));
                    }
                }



            }
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Missing class: " + e.toString());
        }
    }

    private void checkType(Type type) {
        if (type instanceof ArrayType) {
            type = ((ArrayType) type).getBasicType();
        }
        if (type instanceof ObjectType) {
            add(((ObjectType) type).getClassName());
        }
    }

    public String[] getClassNames() {
        return set.getClassNames();
    }


    /**
     * Start traversal using DescendingVisitor pattern.
     */
    public void start() {
        while (!queue.empty()) {
            final JavaClass clazz = queue.dequeue();
            cp = clazz.getConstantPool();

            new DescendingVisitor(clazz, this).visit();
        }
    }

    @Override
    public void visitConstantClass(final ConstantClass cc) {
        final String className = (String) cc.getConstantValue(cp);
        add(className);
    }

    @Override
    public void visitConstantFieldref(final ConstantFieldref cfr) {
        visitRef(cfr, false);
    }

    @Override
    public void visitConstantInterfaceMethodref(final ConstantInterfaceMethodref cimr) {
        visitRef(cimr, true);
    }

    @Override
    public void visitConstantMethodref(final ConstantMethodref cmr) {
        visitRef(cmr, true);
    }
    @Override
    public void visitField(Field f) {
        final String signature = f.getSignature();
        checkType(Type.getType(signature));
    }

    @Override
    public void visitMethod(Method m) {
        final String signature = m.getSignature();
        final Type type = Type.getReturnType(signature);
        checkType(type);
        for (final Type type1 : Type.getArgumentTypes(signature)) {
            checkType(type1);
        }
    }

    private void visitRef(final ConstantCP ccp, final boolean method) {
        final String className = ccp.getClass(cp);
        add(className);
        final ConstantNameAndType cnat = cp.getConstant(ccp.getNameAndTypeIndex(), Const.CONSTANT_NameAndType, ConstantNameAndType.class);
        final String signature = cnat.getSignature(cp);
        if (method) {
            final Type type = Type.getReturnType(signature);
            checkType(type);
            for (final Type type1 : Type.getArgumentTypes(signature)) {
                checkType(type1);
            }
        } else {
            checkType(Type.getType(signature));
        }
    }
}
