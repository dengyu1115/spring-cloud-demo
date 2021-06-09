import org.nature.feign.service.DemoService;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class Test {

    public static void main(String[] args) {
        Class<DemoService> clz = DemoService.class;
        Method[] methods = clz.getMethods();
        for (Method method : methods) {
            Class<?> returnType = method.getReturnType();
            Type superclass = returnType.getGenericSuperclass();
            System.out.println(superclass);
            System.out.println(returnType);
            Type type = method.getGenericReturnType();
            System.out.println(type);
            Class<? extends Type> aClass = type.getClass();
            System.out.println(aClass);
            ParameterizedTypeImpl pti = (ParameterizedTypeImpl) type;
            System.out.println(pti);
            Type[] types = pti.getActualTypeArguments();
            for (Type t : types) {
                System.out.println(t);
            }
        }
    }
}
