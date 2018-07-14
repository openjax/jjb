/* Copyright (c) 2015 lib4j
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.libx4j.jjb.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.lib4j.jci.CompilationException;
import org.lib4j.jci.JavaCompiler;
import org.lib4j.lang.JavaIdentifiers;
import org.lib4j.lang.Strings;
import org.lib4j.math.BigDecimals;
import org.lib4j.xml.ValidationException;
import org.lib4j.xml.dom.DOMStyle;
import org.lib4j.xml.dom.DOMs;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Boolean;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Element;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Named;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Number;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Number.Form$;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Object;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$Property;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.$String;
import org.libx4j.jjb.jsonx_0_9_8.xL0gluGCXYYJc.Json;
import org.libx4j.jjb.runtime.Binding;
import org.libx4j.jjb.runtime.EncodeException;
import org.libx4j.jjb.runtime.JSArray;
import org.libx4j.jjb.runtime.JSBundle;
import org.libx4j.jjb.runtime.JSObject;
import org.libx4j.jjb.runtime.Property;
import org.libx4j.jjb.runtime.Required;
import org.libx4j.jjb.runtime.validator.NumberValidator;
import org.libx4j.jjb.runtime.validator.StringValidator;
import org.libx4j.xsb.runtime.BindingList;
import org.libx4j.xsb.runtime.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {
  private static final Logger logger = LoggerFactory.getLogger(Generator.class);

  public static void main(final String[] args) throws Exception {
    Generator.generate(Thread.currentThread().getContextClassLoader().getResource(args[0]), new File(args[1]), false);
  }

  public static void generate(final URL url, final File destDir, final boolean compile) throws GeneratorExecutionException, IOException, ValidationException {
    final Json json = (Json)Bindings.parse(url);
    if (json.getObject() == null) {
      logger.error("Missing <object> elements: " + url.toExternalForm());
      return;
    }

    final String packageName = "jjb";
    final File outDir = new File(destDir, packageName.replace('.', '/'));
    if (!outDir.exists() && !outDir.mkdirs())
      throw new IOException("Unable to mkdirs: " + outDir.getAbsolutePath());

    for (final Json.Object object : json.getObject())
      objectNameToObject.put(object.getName$().text(), object);

    final String name = json.getName$().text();

    final StringBuilder builder = new StringBuilder();

    builder.append("package ").append(packageName).append(";\n");
    if (json.getDescription() != null)
      builder.append("\n/**\n * ").append(json.getDescription().text()).append("\n */");

    builder.append("\n@").append(SuppressWarnings.class.getName()).append("(\"all\")");
    builder.append("\npublic class ").append(name).append(" extends ").append(JSBundle.class.getName()).append(" {");
    builder.append("\n  public static final ").append(String.class.getName()).append(" mimeType = \"").append(json.getMimeType$().text()).append("\";");
    builder.append("\n  private static ").append(name).append(" instance = null;");
    builder.append("\n\n  protected static ").append(name).append(" instance() {");
    builder.append("\n    return instance == null ? instance = new ").append(name).append("() : instance;");
    builder.append("\n  }");

    builder.append("\n\n  @").append(Override.class.getName());
    builder.append("\n  protected ").append(String.class.getName()).append(" getSpec() {");
    builder.append("\n    return \"").append(DOMs.domToString(json.marshal(), DOMStyle.INDENT).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")).append("\";");
    builder.append("\n  }");

    final Stack<String> parents = new Stack<>();
    parents.push(name);
    for (final Json.Object object : json.getObject())
      builder.append(writeJavaClass(parents, object, 0));

    builder.append("\n\n  private ").append(name).append("() {");
    builder.append("\n  }");
    builder.append("\n}");
    try (final FileOutputStream fos = new FileOutputStream(new File(outDir, name + ".java"))) {
      fos.write(builder.toString().getBytes());
    }

    if (compile) {
      try {
        new JavaCompiler(destDir).compile(destDir);
      }
      catch (final CompilationException e) {
        throw new UnsupportedOperationException(e);
      }
    }
  }

  private static final Map<String,Json.Object> objectNameToObject = new HashMap<>();

  private static String getType(final Stack<String> parents, final $Property property) {
    if (property instanceof $String)
      return String.class.getName();

    if (property instanceof $Number) {
      final $Number numberProperty = ($Number)property;
      if ($Number.Form$.integer.text().equals(numberProperty.getForm$().text()))
        return BigInteger.class.getName();

      if ($Number.Form$.real.text().equals(numberProperty.getForm$().text()))
        return BigDecimal.class.getName();

      throw new UnsupportedOperationException("Unknown number form: " + numberProperty.getForm$().text());
    }

    if (property instanceof $Boolean)
      return Boolean.class.getName();

    if (property instanceof $Object) {
      final $Object object = ($Object)property;
      if (object.getExtends$() != null && !property.elementIterator().hasNext())
        return JavaIdentifiers.toClassCase(object.getExtends$().text());

      final StringBuilder builder = new StringBuilder(parents.get(0));
      for (int i = 1; i < parents.size(); i++)
        builder.append('.').append(JavaIdentifiers.toClassCase(parents.get(i)));

      return builder.append(".").append(JavaIdentifiers.toClassCase((($Object)property).getName$().text())).toString();
    }

    throw new UnsupportedOperationException("Unsupported type: " + property.getClass().getName());
  }

  private static String getPropertyName(final $Property property) {
    if (property instanceof $Named)
      return (($Named)property).getName$().text();

    if (property instanceof $Object)
      return (($Object)property).getName$().text();

    throw new UnsupportedOperationException("Unsupported type: " + property);
  }

  private static String getInstanceName(final $Property property) {
    return JavaIdentifiers.toInstanceCase(getPropertyName(property));
  }

  private static String writeField(final Stack<String> parent, final $Property property, final int depth) {
    final String valueName = getPropertyName(property);
    final boolean isArray = property.getArray$().text();
    final String rawType = getType(parent, property);
    final String type = isArray ? List.class.getName() + "<" + rawType + ">" : rawType;

    final String instanceName = getInstanceName(property);

    final String pad = Strings.padFixed("", depth * 2, false);
    final StringBuilder builder = new StringBuilder("\n");
    if (property.getDescription() != null)
      builder.append("\n").append(pad).append("   /**\n").append(pad).append("    * ").append(property.getDescription().text()).append("\n").append(pad).append("    */");

    builder.append("\n").append(pad).append("   public final ").append(Property.class.getName()).append("<").append(type).append("> ").append(instanceName).append(" = new ").append(Property.class.getName()).append("<").append(type).append(">(this, (").append(Binding.class.getName()).append("<").append(type).append(">)bindings.get(\"").append(valueName).append("\"));");
    return builder.toString();
  }

  private static String writeEncode(final $Property property, final int depth) {
    final String valueName = getPropertyName(property);
    final String instanceName = getInstanceName(property);
    final String pad = Strings.padFixed("", depth * 2, false);

    final StringBuilder builder = new StringBuilder();
    if ("true".equals(property.getRequired$().text()) || "encode".equals(property.getRequired$().text())) {
      builder.append("\n").append(pad).append("     if (!this.").append(instanceName).append(".present())");
      builder.append("\n").append(pad).append("       throw new ").append(EncodeException.class.getName()).append("(_getPath() + \".").append(valueName).append(" is required\", this);\n");
    }

    if (!property.getNull$().text()) {
      builder.append("\n").append(pad).append("     if (this.").append(instanceName).append(".present() && this.").append(instanceName).append(".get() == null && required(this.").append(instanceName).append("))");
      builder.append("\n").append(pad).append("       throw new ").append(EncodeException.class.getName()).append("(_getPath() + \".").append(valueName).append(" cannot be null\", this);\n");
    }

    builder.append("\n").append(pad).append("     if (this.").append(instanceName).append(".present() || required(this.").append(instanceName).append("))");
    builder.append("\n").append(pad).append("       builder.append(delim).append(\"\\\"").append(valueName).append("\\\":\").append(sp).append(");
    if (property.getArray$().text())
      return builder.append(JSArray.class.getName()).append(".toString(encode(this.").append(instanceName).append("), depth == -1 ? -1 : depth + 1));\n").toString();

    if (property instanceof $Object)
      return builder.append("this.").append(instanceName).append(".get() != null ? encode(encode(this.").append(instanceName).append("), depth == -1 ? -1 : depth + 1) : \"null\");\n").toString();

    if (property instanceof $String)
      return builder.append("this.").append(instanceName).append(".get() != null ? \"\\\"\" + encode(this.").append(instanceName).append(") + \"\\\"\" : \"null\");\n").toString();

    return builder.append("encode(this.").append(instanceName).append("));\n").toString();
  }

  private static String writeJavaClass(final Stack<String> parents, final $Element object, final int depth) throws GeneratorExecutionException {
    final String objectName;
    final String extendsPropertyName;
    final boolean skipUnknown;
    final boolean isAbstract;
    final BindingList<$Property> properties;
    if (object instanceof $Object) {
      final $Object object1 = ($Object)object;
      if (object1.getExtends$() != null && !object.elementIterator().hasNext() && !objectNameToObject.get(object1.getExtends$().text()).getAbstract$().text())
        return "";

      objectName = object1.getName$().text();
      extendsPropertyName = object1.getExtends$() != null ? object1.getExtends$().text() : null;
      skipUnknown = $Object.OnUnknown$.skip.text().equals(object1.getOnUnknown$().text());
      isAbstract = false;
      properties = object1.getProperty();
    }
    else if (object instanceof Json.Object) {
      final Json.Object object2 = (Json.Object)object;
      objectName = object2.getName$().text();
      extendsPropertyName = object2.getExtends$() != null ? object2.getExtends$().text() : null;
      skipUnknown = $Object.OnUnknown$.skip.text().equals(object2.getOnUnknown$().text());
      isAbstract = object2.getAbstract$().text();
      properties = object2.getProperty();
    }
    else {
      throw new UnsupportedOperationException("Unsupported object type: " + object.getClass().getName());
    }

    parents.push(objectName);
    final String className = JavaIdentifiers.toClassCase(objectName);

    final String pad = Strings.padFixed("", depth * 2, false);
    final StringBuilder builder = new StringBuilder("\n");
    if (object.getDescription() != null)
      builder.append("\n").append(pad).append(" /**\n").append(pad).append("  * ").append(object.getDescription().text()).append("\n").append(pad).append("  */");

    builder.append("\n").append(pad).append(" public static").append((isAbstract ? " abstract" : "")).append(" class ").append(className).append(" extends ").append((extendsPropertyName != null ? parents.get(0) + "." + JavaIdentifiers.toClassCase(extendsPropertyName) : JSObject.class.getName()) + " {");
    builder.append("\n").append(pad).append("   private static final ").append(String.class.getName()).append(" _name = \"").append(objectName).append("\";\n");
    builder.append("\n").append(pad).append("   private static final ").append(Map.class.getName()).append("<").append(String.class.getName()).append(",").append(Binding.class.getName()).append("<?>> bindings = new ").append(HashMap.class.getName()).append("<").append(String.class.getName()).append(",").append(Binding.class.getName()).append("<?>>(").append((properties != null ? properties.size() : 0)).append(");");

    builder.append("\n").append(pad).append("   static {");
    builder.append("\n").append(pad).append("     registerBinding(_name, ").append(className).append(".class);");
    if (properties != null) {
      builder.append("\n").append(pad).append("     try {");
      for (final $Property property : properties) {
        final String propertyName = getPropertyName(property);
        final String rawType = getType(parents, property);
        final boolean isArray = property.getArray$().text();
        final String type = isArray ? List.class.getName() + "<" + rawType + ">" : rawType;

        builder.append("\n").append(pad).append("       bindings.put(\"").append(propertyName).append("\", new ").append(Binding.class.getName()).append("<").append(type).append(">(\"").append(propertyName).append("\", ").append(className).append(".class.getDeclaredField(\"").append(getInstanceName(property)).append("\"), ").append(rawType).append(".class, ").append(isAbstract).append(", ").append(isArray).append(", ").append(Required.class.getName()).append(".").append(property.getRequired$().text().toUpperCase()).append(", ").append(!property.getNull$().text()).append((property instanceof $String ? ", " + (($String)property).getUrlDecode$().text() + ", " + (($String)property).getUrlEncode$().text() : ""));
        if (property instanceof $String) {
          final $String string = ($String)property;
          if (string.getPattern$() != null || string.getLength$() != null)
            builder.append(", new ").append(StringValidator.class.getName()).append("(").append((string.getPattern$() == null ? "null" : "\"" + XMLText.unescapeXMLText(string.getPattern$().text()).replace("\\", "\\\\").replace("\"", "\\\"") + "\"")).append(", ").append((string.getLength$() == null ? "null" : string.getLength$().text())).append(")");
        }
        else if (property instanceof $Number) {
          final $Number number = ($Number)property;
          if (Form$.integer.text().equals(number.getForm$().text()) || number.getMin$() != null || number.getMax$() != null) {
            if (number.getMin$() != null && number.getMax$() != null && number.getMin$().text().compareTo(number.getMax$().text()) > 0)
              throw new GeneratorExecutionException("min (" + number.getMin$().text() + ") > max (" + number.getMax$().text() + ") on property: " + objectName + "." + propertyName);

            builder.append(", new ").append(NumberValidator.class.getName()).append("(").append(Form$.integer.text().equals(number.getForm$().text())).append(", ").append((number.getMin$() == null ? "null" : (BigDecimals.class.getName() + ".instance(\"" +  number.getMin$().text().stripTrailingZeros().toPlainString()) + "\")")).append(", ").append($Number.MinBound$.inclusive.text().equals(number.getMinBound$().text())).append(", ").append((number.getMax$() == null ? "null" : (BigDecimals.class.getName() + ".instance(\"" +  number.getMax$().text().stripTrailingZeros().toPlainString()) + "\")")).append(", ").append($Number.MinBound$.inclusive.text().equals(number.getMaxBound$().text())).append(")");
          }
        }

        builder.append("));");
      }

      builder.append("\n").append(pad).append("     }");
      builder.append("\n").append(pad).append("     catch (final ").append(ReflectiveOperationException.class.getName()).append(" e) {");
      builder.append("\n").append(pad).append("       throw new ").append(ExceptionInInitializerError.class.getName()).append("(e);");
      builder.append("\n").append(pad).append("     }");
    }
    builder.append("\n").append(pad).append("   }");

    if (properties != null)
      for (final $Property property : properties)
        if (property instanceof $Object)
          builder.append(writeJavaClass(parents, property, depth + 1));

    builder.append("\n\n").append(pad).append("   public ").append(className).append("(final ").append(JSObject.class.getName()).append(" object) {");
    builder.append("\n").append(pad).append("     super(object);");
    if (properties != null) {
      builder.append("\n").append(pad).append("     if (!(object instanceof ").append(className).append("))");
      builder.append("\n").append(pad).append("       return;");
      builder.append("\n\n").append(pad).append("     final ").append(className).append(" that = (").append(className).append(")object;");
      for (final $Property property : properties) {
        final String instanceName = getInstanceName(property);
        builder.append("\n").append(pad).append("     clone(this.").append(instanceName).append(", that.").append(instanceName).append(");");
      }
    }

    builder.append("\n").append(pad).append("   }");

    builder.append("\n\n").append(pad).append("   public ").append(className).append("() {");
    builder.append("\n").append(pad).append("     super();");
    builder.append("\n").append(pad).append("   }");

    if (!isAbstract) {
      builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
      builder.append("\n").append(pad).append("   protected ").append(String.class.getName()).append(" _getPath() {");
      final StringBuilder path = new StringBuilder(parents.get(1));
      for (int i = 2; i < parents.size(); i++)
        path.append('.').append(parents.get(i));

      builder.append("\n").append(pad).append("     return \"").append(path).append("\";");
      builder.append("\n").append(pad).append("   }\n");
    }

    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   protected boolean _skipUnknown() {");
    builder.append("\n").append(pad).append("     return ").append(skipUnknown).append(";");
    builder.append("\n").append(pad).append("   }\n");

    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   protected ").append(Binding.class.getName()).append("<?> _getBinding(final ").append(String.class.getName()).append(" name) {");
    if (extendsPropertyName != null) {
      builder.append("\n").append(pad).append("     final ").append(Binding.class.getName()).append(" binding = super._getBinding(name);");
      builder.append("\n").append(pad).append("     return binding != null ? binding : bindings.get(name);");
    }
    else {
      builder.append("\n").append(pad).append("     return bindings.get(name);");
    }
    builder.append("\n").append(pad).append("   }\n");
    builder.append("\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   protected ").append(Collection.class.getName()).append("<").append(Binding.class.getName()).append("<?>> _bindings() {");
    if (extendsPropertyName != null) {
      builder.append("\n").append(pad).append("     final ").append(List.class.getName()).append(" bindings = new ").append(ArrayList.class.getName()).append("<").append(Binding.class.getName()).append("<?>>();");
      builder.append("\n").append(pad).append("     bindings.addAll(super._bindings());");
      builder.append("\n").append(pad).append("     bindings.addAll(this.bindings.values());");
      builder.append("\n").append(pad).append("     return bindings;");
    }
    else {
      builder.append("\n").append(pad).append("     return bindings.values();");
    }
    builder.append("\n").append(pad).append("   }");
    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   protected ").append(JSBundle.class.getName()).append(" _bundle() {");
    builder.append("\n").append(pad).append("     return ").append(parents.get(0)).append(".instance();");
    builder.append("\n").append(pad).append("   }");
    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   protected ").append(String.class.getName()).append(" _name() {");
    builder.append("\n").append(pad).append("     return _name;");
    builder.append("\n").append(pad).append("   }");
    if (properties != null) {
      for (final $Property property : properties)
        builder.append(writeField(parents, property, depth));

      builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
      builder.append("\n").append(pad).append("   protected ").append(String.class.getName()).append(" _encode(final int depth) {");
      builder.append("\n").append(pad).append("     final ").append(StringBuilder.class.getName()).append(" builder = new ").append(StringBuilder.class.getName()).append("(super._encode(depth));");
      builder.append("\n").append(pad).append("     final int startLength = builder.length();");
      builder.append("\n").append(pad).append("     if (depth == -1 && startLength == 0)");
      builder.append("\n").append(pad).append("       builder.append(' ');");
      builder.append("\n").append(pad).append("     final String sp = depth > -1 ? \" \" : \"\";");
      builder.append("\n").append(pad).append("     final String delim = depth > -1 ? \",\\n\" + pad(depth) : \",\";");
      for (int i = 0; i < properties.size(); i++)
        builder.append(writeEncode(properties.get(i), depth));

      builder.append("\n").append(pad).append("     return startLength == builder.length() || startLength != 0 ? builder.toString() : builder.substring(2);\n").append(pad).append("   }");
    }

    if (!isAbstract) {
      builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
      builder.append("\n").append(pad).append("   public ").append(className).append(" clone() {");
      builder.append("\n").append(pad).append("     return new ").append(className).append("(this);");
      builder.append("\n").append(pad).append("   }");
    }

    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   public boolean equals(final ").append(Object.class.getName()).append(" obj) {");
    builder.append("\n").append(pad).append("     if (obj == this)");
    builder.append("\n").append(pad).append("       return true;");
    builder.append("\n\n").append(pad).append("     if (!(obj instanceof ").append(className).append(")").append((extendsPropertyName != null ? " || !super.equals(obj)" : "")).append(")");
    builder.append("\n").append(pad).append("       return false;\n");
    if (properties != null) {
      builder.append("\n").append(pad).append("     final ").append(className).append(" that = (").append(className).append(")obj;");
      for (final $Property property : properties) {
        final String instanceName = getInstanceName(property);
        builder.append("\n").append(pad).append("     if (that.").append(instanceName).append(" != null ? !that.").append(instanceName).append(".equals(").append(instanceName).append(") : ").append(instanceName).append(" != null)");
        builder.append("\n").append(pad).append("       return false;\n");
      }
    }
    builder.append("\n").append(pad).append("     return true;");
    builder.append("\n").append(pad).append("   }");

    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   public int hashCode() {");
    if (properties != null) {
      builder.append("\n").append(pad).append("     int hashCode = ").append(className.hashCode()).append((extendsPropertyName != null ? " ^ 31 * super.hashCode()" : "")).append(";");
      for (final $Property property : properties) {
        final String instanceName = getInstanceName(property);
        builder.append("\n").append(pad).append("     if (").append(instanceName).append(" != null)");
        builder.append("\n").append(pad).append("       hashCode ^= 31 * ").append(instanceName).append(".hashCode();\n");
      }
      builder.append("\n").append(pad).append("     return hashCode;");
    }
    else {
      builder.append("\n").append(pad).append("     return ").append(className.hashCode()).append((extendsPropertyName != null ? " ^ 31 * super.hashCode()" : "")).append(";");
    }
    builder.append("\n").append(pad).append("   }");

    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   public ").append(String.class.getName()).append(" toString() {");
    builder.append("\n").append(pad).append("     return encode(this, 1);");
    builder.append("\n").append(pad).append("   }");

    builder.append("\n\n").append(pad).append("   @").append(Override.class.getName());
    builder.append("\n").append(pad).append("   public ").append(String.class.getName()).append(" toExternalForm() {");
    builder.append("\n").append(pad).append("     return encode(this, -1);");
    builder.append("\n").append(pad).append("   }");

    builder.append("\n").append(pad).append(" }");

    parents.pop();
    return builder.toString();
  }
}