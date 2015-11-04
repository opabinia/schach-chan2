package org.rorschach.complex

import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.*

public class ValidationDriver<V, T> {

    public fun runValidation(validator: V, target:T) : Boolean{
        val a = accumulateDepends(getMarkedMethods(validator as Any)
                .map(::buildFieldDependFromSignature))

        val order = solveDepend(a)

        for(t in order) {
            val methods = getMarkedMethods(validator)
                    .filter { buildFieldDependFromSignature(it).target.equals(t) }

            for(m in methods) {
                if(!(m.invoke(validator, *buildParameter(signatureToFieldName(m), target as Any)) as Boolean))
                    return false
            }
        }

        return true
    }

}

private fun getMarkedMethods(value: Any): List<Method> {
    return value.javaClass.methods.filter{it.getAnnotationsByType(Verifier::class.java).size != 0}.toList()
}

private fun getParameterIndicatedFieldName(parameter: Parameter):String {
    if(parameter.getAnnotationsByType(Target::class.java).size != 0) {
        return parameter.getAnnotationsByType(Target::class.java).first().value
    }
    else {
        return parameter.getAnnotationsByType(Depend::class.java).first().value
    }
}

private fun buildFieldDependFromSignature(method: Method): FieldDepend {
    val p = method.parameters
    val depend = FieldDepend(getParameterIndicatedFieldName(p[0]))
    p.drop(1).map(::getParameterIndicatedFieldName).forEach { depend.depend.add(it) }
    return depend
}

private fun accumulateDepends(depends: List<FieldDepend>): List<FieldDepend> {
    val s = HashSet<String>()
    depends.forEach { s.add(it.target); s.addAll(it.depend) }

    val newDependMap = HashMap<String, FieldDepend>()
    s.forEach { newDependMap[it] = FieldDepend(it) }

    depends.forEach { newDependMap[it.target]?.depend?.addAll(it.depend) }
    return newDependMap.values.toList()
}

private fun solveDepend(depends: List<FieldDepend>): List<String> {
    val order = ArrayList<String>()
    order.addAll(depends.filter { it.depend.size == 0 }.map { it.target })

    val dep = ArrayList<FieldDepend>()
    dep.addAll(depends.filter { it.depend.size != 0 })

    while (dep.size != 0) {
        for (d in dep) {
            if (order.containsAll(d.depend)) {
                order.add(d.target)
            }
        }
        for (o in order) {
            dep.removeIf { it.target.equals(o) }
        }
    }
    return order
}

private fun getFieldValueViaGetter(fieldName: String, value : Any) : Any {
    val getter = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1)
    val method = value.javaClass.getMethod(getter)
    return method.invoke(value)
}

private fun buildParameter(field: List<String>, obj: Any):Array<Any> {
    return field.map { getFieldValueViaGetter(it, obj) }.toTypedArray()
}

private fun signatureToFieldName(method: Method): List<String> {
    return method.parameters.map(::getParameterIndicatedFieldName)
}