package com.calvin.android.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-8
 */
public class Reflect {

    private static final String TAG = "Reflect";

    public static void main(String[] args) {
        String str = "abc";
        Class c1 = str.getClass();

        try {
            Class c2 = Class.forName("java.lang.String");

            Class c3 = Class.forName("android.widget.Button");

            Class c5 = c3.getSuperclass();//得到TextView
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Class c6 = String.class;
        Class c9 = int.class;
        Class c10 = int[].class;


        Class c11 = Boolean.TYPE;
        Class c12 = Byte.TYPE;
        Class c13 = Character.TYPE;
        Class c14 = Integer.TYPE;


        Class testClassCtor = TestClassCtor.class;

        String className = testClassCtor.getName();

        try {
            Constructor[] constructors = testClassCtor.getDeclaredConstructors();

            for (int i=0; i< constructors.length; i++){
                int mod = constructors[i].getModifiers();
                System.out.println("modifier = "+ Modifier.toString(mod)+" "+className+" (");

                Class[] parameterTypes = constructors[i].getParameterTypes();
                for (int j =0 ; j<parameterTypes.length; j++){
                    System.out.println( parameterTypes[j].getName());
                    if (parameterTypes.length > j+1){
                        System.out.println( ", ");
                    }
                }
                System.out.println( ")");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public class TestClassCtor {
        String name;
        public TestClassCtor(){
            name = "calvin";
        }

        public TestClassCtor(int a){

        }

        public TestClassCtor(int a, String b){
            name = b;
        }

        private TestClassCtor(int a, double b){

        }
    }
}
