package com.calvin.android.modulebus;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.lang.model.element.Modifier;


/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
public class JavapeotExample {


    /*package com.example.helloworld;

    public final class HelloWorld {
        public static void main(String[] args) {
            System.out.println("Hello, JavaPoet!");
        }
    }*/

    public void generateHelloWord(){

        try {
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    /*生成如下代码段
                    int total = 0;
                    for (int i = 0; i < 10; i++) {
                        total += i;
                    }*/
                    .addCode(""
                        + "int total = 0;\n"
                        + "for (int i = 0; i < 10; i++) {\n"
                        + "  total += i;\n"
                        + "}\n")
                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                    .build();

            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                    .build();

            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //通过addStatement() + beginControlFlow() + endControlFlow() 形式输出
    public void generateControlFlow(){
        try {
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(int.class)
                    .addStatement("int total = 0")
                    .beginControlFlow("for (int i = 0; i < 10; i++)")
                    .addStatement("total += i")
                    .endControlFlow()
                    .addStatement("return 0;")
                    .build();

            TypeSpec controlFlow = TypeSpec.classBuilder("ControlFlow")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.example.helloworld", controlFlow)
                    .build();

            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //控制流参数设置
    private MethodSpec computeRange(String name, int from, int to, String op) {
        return MethodSpec.methodBuilder(name)
                .returns(int.class)
                .addStatement("int result = 1")
                .beginControlFlow("for (int i = " + from + "; i < " + to + "; i++)")
                .addStatement("result = result " + op + " i")
                .endControlFlow()
                .addStatement("return result")
                .build();
    }

    /* 调用 computeRange("multiply10to20", 10, 20, "*") 会生成如下代码

    int multiply10to20() {
        int result = 1;
        for (int i = 10; i < 20; i++) {
            result = result * i;
        }
        return result;
    }*/

}
