package com.calvin.android.module.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-5
 */
final class ViewBinding {
    private final Id id;
    //private final Map<ListenerClass, Map<ListenerMethod, Set<MethodViewBinding>>> methodBindings;

    private final FieldViewBinding fieldBinding;

    ViewBinding(Id id, /*Map<ListenerClass, Map<ListenerMethod, Set<MethodViewBinding>>> methodBindings,*/ FieldViewBinding fieldBinding) {
        this.id = id;
        //this.methodBindings = methodBindings;
        this.fieldBinding = fieldBinding;
    }

    public Id getId() {
        return this.id;
    }

    public FieldViewBinding getFieldBinding() {
        return this.fieldBinding;
    }

    /*public Map<ListenerClass, Map<ListenerMethod, Set<MethodViewBinding>>> getMethodBindings() {
        return this.methodBindings;
    }*/

    public List<MemberViewBinding> getRequiredBindings() {
        List<MemberViewBinding> requiredBindings = new ArrayList();
        if (this.fieldBinding != null && this.fieldBinding.isRequired()) {
            requiredBindings.add(this.fieldBinding);
        }

        /*Iterator var2 = this.methodBindings.values().iterator();

        while(var2.hasNext()) {
            Map<ListenerMethod, Set<MethodViewBinding>> methodBinding = (Map)var2.next();
            Iterator var4 = methodBinding.values().iterator();

            while(var4.hasNext()) {
                Set<MethodViewBinding> set = (Set)var4.next();
                Iterator var6 = set.iterator();

                while(var6.hasNext()) {
                    MethodViewBinding binding = (MethodViewBinding)var6.next();
                    if (binding.isRequired()) {
                        requiredBindings.add(binding);
                    }
                }
            }
        }*/

        return requiredBindings;
    }

    public boolean isSingleFieldBinding() {
        return /*this.methodBindings.isEmpty() &&*/ this.fieldBinding != null;
    }

    public boolean requiresLocal() {
        if (this.isBoundToRoot()) {
            return false;
        } else {
            return !this.isSingleFieldBinding();
        }
    }

    public boolean isBoundToRoot() {
        return ViewInjectProcessor.NO_ID.equals(this.id);
    }

    public static final class Builder {
        private final Id id;
        //private final Map<ListenerClass, Map<ListenerMethod, Set<MethodViewBinding>>> methodBindings = new LinkedHashMap();

        FieldViewBinding fieldBinding;

        Builder(Id id) {
            this.id = id;
        }

        /*public boolean hasMethodBinding(ListenerClass listener, ListenerMethod method) {
            Map<ListenerMethod, Set<MethodViewBinding>> methods = (Map)this.methodBindings.get(listener);
            return methods != null && methods.containsKey(method);
        }*/

        /*public void addMethodBinding(ListenerClass listener, ListenerMethod method, MethodViewBinding binding) {
            Map<ListenerMethod, Set<MethodViewBinding>> methods = (Map)this.methodBindings.get(listener);
            Set<MethodViewBinding> set = null;
            if (methods == null) {
                methods = new LinkedHashMap();
                this.methodBindings.put(listener, methods);
            } else {
                set = (Set)((Map)methods).get(method);
            }

            if (set == null) {
                set = new LinkedHashSet();
                ((Map)methods).put(method, set);
            }

            ((Set)set).add(binding);
        }*/

        public void setFieldBinding(FieldViewBinding fieldBinding) {
            if (this.fieldBinding != null) {
                throw new AssertionError();
            } else {
                this.fieldBinding = fieldBinding;
            }
        }

        public ViewBinding build() {
            return new ViewBinding(this.id, /*this.methodBindings,*/ this.fieldBinding);
        }
    }
}
