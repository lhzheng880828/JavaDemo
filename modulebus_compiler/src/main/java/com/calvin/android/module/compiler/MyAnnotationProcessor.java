package com.calvin.android.module.compiler;

import com.calvin.android.module.annotation.ApiInject;
import com.calvin.android.module.compiler.utils.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 每一个注解处理器类都必须有一个空的构造函数，默认不写就行;
 */

//这里指定的是编译的JDK版本号
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
//这里指定要解析的自定义注解
//@SupportedAnnotationTypes("com.example.annotation.cls.MyAnnotation")
//添加这个注解后，编译器可以自动帮我们在main/resources/META-INF中添加service文件
//@AutoService(MyAnnotationProcessor.class)
public class MyAnnotationProcessor extends AbstractProcessor {

    private Filer mFiler;       // File util, write class file into disk.
    private Logger logger;
    private Types types;
    private Elements elements;

    /**
     * init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
     * ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
     * @param processingEnv 提供给 processor 用来访问工具框架的环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();                  // Generate class.
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.
        System.out.println("------------------------------");
        System.out.println("ModuleProcess init");
    }

    /**
     * 这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
     * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
     * @param annotations   请求处理的注解类型
     * @param roundEnv  有关当前和以前的信息环境
     * @return  如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们；
     *          如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, String> packageTagMap = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(ApiInject.class)) {
            System.out.println("------------------------------");


            // 判断元素的类型为Class
            if (element.getKind() == ElementKind.CLASS) {
                // 显示转换元素类型
                TypeElement typeElement = (TypeElement) element;
                // 输出元素名称
                Name packageName = typeElement.getQualifiedName();
                System.out.println(packageName);
                System.out.println(typeElement.getSimpleName());
                // 输出注解属性值
                String tag = typeElement.getAnnotation(ApiInject.class).value();
                System.out.println(tag);
                packageTagMap.put(packageName.toString(), tag);
            }
            System.out.println("------------------------------");
        }

        int mapSize = packageTagMap.size();

        if(mapSize>0){

            StringBuilder mapGenerateCode = new StringBuilder();
            //packagename, key
            Iterator<Map.Entry<String, String>> iterator = packageTagMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String getApi = String.format("BsIApi %s = getBsApi(\"%s\");\n", entry.getValue()+"api", entry.getKey());
                String getKey = String.format("String %s = \"%s\";\n", entry.getValue(), entry.getValue());
                String putMap = String.format("apiMap.put(%s, %s);\n", entry.getValue(), entry.getValue()+"api");
                mapGenerateCode.append(getApi);
                mapGenerateCode.append(getKey);
                mapGenerateCode.append(putMap);
            }



            try {

                ClassName baseApi = ClassName.get(elements.getTypeElement(ModuleUtils.BS_API));

                logger.warning("baseApi = "+baseApi.reflectionName());

                MethodSpec apiInject1 = MethodSpec.methodBuilder("apiInject")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addCode("apiMap = new HashMap<String, BsIApi>();\n")
                        .addStatement(mapGenerateCode.toString())
                        .build();

                MethodSpec getApi = MethodSpec.methodBuilder("getBsApi")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .addParameter(String.class, "packageName")
                        .addCode("try {\n" +
                                "Class<?> c = Class.forName(packageName);\n" +
                                "     return (BsIApi) c.newInstance();\n" +
                                "} catch (ClassNotFoundException e) {\n" +
                                " if (DEBUG) System.out.println(TAG+\", getApiIApi from \" + packageName + \",error=\" + e.toString());\n" +
                                "} catch (InstantiationException e) {\n" +
                                "    if (DEBUG) System.out.println(TAG+\", getApiIApi from \" + packageName + \",error=\" + e.toString());\n" +
                                "} catch (IllegalAccessException e) {\n" +
                                "    if (DEBUG) System.out.println(TAG+\", getApiIApi from \" + packageName + \",error=\" + e.toString());\n" +
                                "}\n" +
                                "return null;")
                        .returns(baseApi)
                        .build();


                ParameterizedTypeName inputMapTypeOfApi = ParameterizedTypeName.get(
                        ClassName.get(HashMap.class),
                        ClassName.get(String.class),
                        baseApi
                );

                TypeSpec apiInject = TypeSpec.classBuilder("ApiInject")
                        .addJavadoc(ModuleUtils.WARNING_TIPS)
                        //添加集成接口
                        //.addSuperinterface(ClassName.get(elements.getTypeElement(ModuleUtil.IMODULE_UNIT)))
                        .addModifiers(Modifier.PUBLIC)
                        .addField(FieldSpec.builder(boolean.class,"DEBUG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("true").build())
                        .addField(FieldSpec.builder(String.class, "TAG",Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$S", "ApiClient").build())
                        .addField(FieldSpec.builder(inputMapTypeOfApi, "apiMap",Modifier.PRIVATE, Modifier.STATIC).build())
                        .addMethod(getApi)
                        .addMethod(apiInject1)
                        .build();


                //构造java文件
                JavaFile.builder(ModuleUtils.FACADE_PACKAGENAME,apiInject).build().writeTo(mFiler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

            /* 待生成的代码块

            package com.calvin.android.modulebus;

            import java.lang.reflect.Field;
            import java.util.HashMap;
            import java.util.Map;

            public static class ApiInject{

                private static Map<String, BsIApi> apiMap;

                public static void apiInject(){
                    apiMap = new HashMap<String, BsIApi>();
                    BsIApi contactApi = getBsApi("com.calvin.android.modulebus.BsContactsApiImpl");
                    String contactApiKey = getBsApiKey("com.calvin.android.modulebus.ContactsApi");
                    apiMap.put(contactApiKey, contactApi);

                    BsIApi callLogApi = getBsApi("com.calvin.android.modulebus.BsCalllogApiImpl");
                    String callLogApiKey = getBsApiKey("com.calvin.android.modulebus.CalllogApi");
                    apiMap.put(callLogApiKey, callLogApi);
                }

                private static BsIApi getBsApi(String packageName) {
                    try {
                        Class<?> c = Class.forName(packageName);
                        return (BsIApi) c.newInstance();
                    } catch (ClassNotFoundException e) {
                        if (DEBUG) System.out.println(TAG+", getApiIApi from " + packageName + ",error=" + e.toString());
                    } catch (InstantiationException e) {
                        if (DEBUG) System.out.println(TAG+", getApiIApi from " + packageName + ",error=" + e.toString());
                    } catch (IllegalAccessException e) {
                        if (DEBUG) System.out.println(TAG+", getApiIApi from " + packageName + ",error=" + e.toString());
                    }
                    return null;
                }


            }*/

        return false;
    }

    /**
     * 这里必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称
     * @return  注解器所支持的注解类型集合，如果没有这样的类型，则返回一个空集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(ApiInject.class.getCanonicalName());
        return annotataions;
    }

    /**
     * 指定使用的Java版本，通常这里返回SourceVersion.latestSupported()，默认返回SourceVersion.RELEASE_6
     * @return  使用的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
