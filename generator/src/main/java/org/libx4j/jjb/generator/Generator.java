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
import org.lib4j.lang.Resources;
import org.lib4j.lang.Strings;
import org.lib4j.math.BigDecimals;
import org.lib4j.util.JavaIdentifiers;
import org.lib4j.xml.XMLText;
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
    Generator.generate(Resources.getResource(args[0]).getURL(), new File(args[1]), false);
  }

  public static void generate(final URL url, final File destDir, final boolean compile) throws GeneratorExecutionException, IOException {
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

    String out = "";

    out += "package " + packageName + ";\n";
    if (json.getDescription() != null)
      out += "\n/**\n * " + json.getDescription().text() + "\n */";

    out += "\n@" + SuppressWarnings.class.getName() + "(\"all\")";
    out += "\npublic class " + name + " extends " + JSBundle.class.getName() + " {";
    out += "\n  public static final " + String.class.getName() + " mimeType = \"" + json.getMimeType$().text() + "\";";
    out += "\n  private static " + name + " instance = null;";
    out += "\n\n  protected static " + name + " instance() {";
    out += "\n    return instance == null ? instance = new " + name + "() : instance;";
    out += "\n  }";

    out += "\n\n  @" + Override.class.getName();
    out += "\n  protected " + String.class.getName() + " getSpec() {";
    out += "\n    return \"" + DOMs.domToString(json.marshal(), DOMStyle.INDENT).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\";";
    out += "\n  }";

    final Stack<String> parents = new Stack<String>();
    parents.push(name);
    for (final Json.Object object : json.getObject())
      out += writeJavaClass(parents, object, 0);

    out += "\n\n  private " + name + "() {";
    out += "\n  }";
    out += "\n}";
    try (final FileOutputStream fos = new FileOutputStream(new File(outDir, name + ".java"))) {
      fos.write(out.toString().getBytes());
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

  private static final Map<String,Json.Object> objectNameToObject = new HashMap<String,Json.Object>();

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

      return builder + "." + JavaIdentifiers.toClassCase((($Object)property).getName$().text());
    }

    throw new UnsupportedOperationException("Unknown type: " + property.getClass().getName());
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
    String out = "\n";
    if (property.getDescription() != null)
      out += "\n" + pad + "   /**\n" + pad + "    * " + property.getDescription().text() + "\n" + pad + "    */";

    out += "\n" + pad + "   public final " + Property.class.getName() + "<" + type + "> " + instanceName + " = new " + Property.class.getName() + "<" + type + ">(this, (" + Binding.class.getName() + "<" + type + ">)bindings.get(\"" + valueName + "\"));";
    return out;
  }

  private static String writeEncode(final $Property property, final int depth) {
    final String valueName = getPropertyName(property);
    final String instanceName = getInstanceName(property);
    final String pad = Strings.padFixed("", depth * 2, false);

    String out = "";
    if ("true".equals(property.getRequired$().text()) || "encode".equals(property.getRequired$().text())) {
      out += "\n" + pad + "     if (!" + instanceName + ".present())";
      out += "\n" + pad + "       throw new " + EncodeException.class.getName() + "(_getPath() + \"." + valueName + " is required\", this);\n";
    }

    if (!property.getNull$().text()) {
      out += "\n" + pad + "     if (" + instanceName + ".present() && " + instanceName + ".get() == null)";
      out += "\n" + pad + "       throw new " + EncodeException.class.getName() + "(_getPath() + \"." + valueName + " cannot be null\", this);\n";
    }

    out += "\n" + pad + "     if (" + instanceName + ".present() || required(" + instanceName + "))";
    out += "\n" + pad + "       out.append(\",\\n\").append(pad(depth)).append(\"\\\"" + valueName + "\\\": \").append(";
    if (property.getArray$().text())
      return out + JSArray.class.getName() + ".toString(encode(" + instanceName + "), depth + 1));\n";

    if (property instanceof $Object)
      return out + "" + instanceName + ".get() != null ? encode(encode(" + instanceName + "), depth + 1) : \"null\");\n";

    if (property instanceof $String)
      return out + "" + instanceName + ".get() != null ? \"\\\"\" + encode(" + instanceName + ") + \"\\\"\" : \"null\");\n";

    return out + "encode(" + instanceName + "));\n";
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
    String out = "\n";
    if (object.getDescription() != null)
      out += "\n" + pad + " /**\n" + pad + "  * " + object.getDescription().text() + "\n" + pad + "  */";

    out += "\n" + pad + " public static" + (isAbstract ? " abstract" : "") + " class " + className + " extends " + (extendsPropertyName != null ? parents.get(0) + "." + JavaIdentifiers.toClassCase(extendsPropertyName) : JSObject.class.getName()) + " {";
    out += "\n" + pad + "   private static final " + String.class.getName() + " _name = \"" + objectName + "\";\n";
    out += "\n" + pad + "   private static final " + Map.class.getName() + "<" + String.class.getName() + "," + Binding.class.getName() + "<?>> bindings = new " + HashMap.class.getName() + "<" + String.class.getName() + "," + Binding.class.getName() + "<?>>(" + (properties != null ? properties.size() : 0) + ");";

    out += "\n" + pad + "   static {";
    out += "\n" + pad + "     registerBinding(_name, " + className + ".class);";
    if (properties != null) {
      out += "\n" + pad + "     try {";
      for (final $Property property : properties) {
        final String propertyName = getPropertyName(property);
        final String rawType = getType(parents, property);
        final boolean isArray = property.getArray$().text();
        final String type = isArray ? List.class.getName() + "<" + rawType + ">" : rawType;

        out += "\n" + pad + "       bindings.put(\"" + propertyName + "\", new " + Binding.class.getName() + "<" + type + ">(\"" + propertyName + "\", " + className + ".class.getDeclaredField(\"" + getInstanceName(property) + "\"), " + rawType + ".class, " + isAbstract + ", " + isArray + ", " + Required.class.getName() + "." + property.getRequired$().text().toUpperCase() + ", " + !property.getNull$().text() + (property instanceof $String ? ", " + (($String)property).getUrlDecode$().text() + ", " + (($String)property).getUrlEncode$().text() : "");
        if (property instanceof $String) {
          final $String string = ($String)property;
          if (string.getPattern$() != null || string.getLength$() != null)
            out += ", new " + StringValidator.class.getName() + "(" + (string.getPattern$() == null ? "null" : "\"" + XMLText.unescapeXMLText(string.getPattern$().text()).replace("\\", "\\\\").replace("\"", "\\\"") + "\"") + ", " + (string.getLength$() == null ? "null" : string.getLength$().text()) + ")";
        }
        else if (property instanceof $Number) {
          final $Number number = ($Number)property;
          if (Form$.integer.text().equals(number.getForm$().text()) || number.getMin$() != null || number.getMax$() != null) {
            if (number.getMin$() != null && number.getMax$() != null && number.getMin$().text().compareTo(number.getMax$().text()) > 0)
              throw new GeneratorExecutionException("min (" + number.getMin$().text() + ") > max (" + number.getMax$().text() + ") on property: " + objectName + "." + propertyName);

            out += ", new " + NumberValidator.class.getName() + "(" + Form$.integer.text().equals(number.getForm$().text()) + ", " + (number.getMin$() == null ? "null" : (BigDecimals.class.getName() + ".instance(\"" +  number.getMin$().text().stripTrailingZeros().toPlainString()) + "\")") + ", " + $Number.MinBound$.inclusive.text().equals(number.getMinBound$().text()) + ", " + (number.getMax$() == null ? "null" : (BigDecimals.class.getName() + ".instance(\"" +  number.getMax$().text().stripTrailingZeros().toPlainString()) + "\")") + ", " + $Number.MinBound$.inclusive.text().equals(number.getMaxBound$().text()) + ")";
          }
        }

        out += "));";
      }

      out += "\n" + pad + "     }";
      out += "\n" + pad + "     catch (final " + ReflectiveOperationException.class.getName() + " e) {";
      out += "\n" + pad + "       throw new " + ExceptionInInitializerError.class.getName() + "(e);";
      out += "\n" + pad + "     }";
    }
    out += "\n" + pad + "   }";

    if (properties != null)
      for (final $Property property : properties)
        if (property instanceof $Object)
          out += writeJavaClass(parents, property, depth + 1);

    out += "\n\n" + pad + "   public " + className + "(final " + JSObject.class.getName() + " object) {";
    out += "\n" + pad + "     super(object);";
    if (properties != null) {
      out += "\n" + pad + "     if (!(object instanceof " + className + "))";
      out += "\n" + pad + "       return;";
      out += "\n\n" + pad + "     final " + className + " that = (" + className + ")object;";
      for (final $Property property : properties) {
        final String instanceName = getInstanceName(property);
        out += "\n" + pad + "     clone(this." + instanceName + ", that." + instanceName + ");";
      }
    }

    out += "\n" + pad + "   }";

    out += "\n\n" + pad + "   public " + className + "() {";
    out += "\n" + pad + "     super();";
    out += "\n" + pad + "   }";

    if (!isAbstract) {
      out += "\n\n" + pad + "   @" + Override.class.getName();
      out += "\n" + pad + "   protected " + String.class.getName() + " _getPath() {";
      final StringBuilder builder = new StringBuilder(parents.get(1));
      for (int i = 2; i < parents.size(); i++)
        builder.append('.').append(parents.get(i));

      out += "\n" + pad + "     return \"" + builder + "\";";
      out += "\n" + pad + "   }\n";
    }

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   protected boolean _skipUnknown() {";
    out += "\n" + pad + "     return " + skipUnknown + ";";
    out += "\n" + pad + "   }\n";

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   protected " + Binding.class.getName() + "<?> _getBinding(final " + String.class.getName() + " name) {";
    if (extendsPropertyName != null) {
      out += "\n" + pad + "     final " + Binding.class.getName() + " binding = super._getBinding(name);";
      out += "\n" + pad + "     return binding != null ? binding : bindings.get(name);";
    }
    else {
      out += "\n" + pad + "     return bindings.get(name);";
    }
    out += "\n" + pad + "   }\n";
    out += "\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   protected " + Collection.class.getName() + "<" + Binding.class.getName() + "<?>> _bindings() {";
    if (extendsPropertyName != null) {
      out += "\n" + pad + "     final " + List.class.getName() + " bindings = new " + ArrayList.class.getName() + "<" + Binding.class.getName() + "<?>>();";
      out += "\n" + pad + "     bindings.addAll(super._bindings());";
      out += "\n" + pad + "     bindings.addAll(this.bindings.values());";
      out += "\n" + pad + "     return bindings;";
    }
    else {
      out += "\n" + pad + "     return bindings.values();";
    }
    out += "\n" + pad + "   }";
    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   protected " + JSBundle.class.getName() + " _bundle() {";
    out += "\n" + pad + "     return " + parents.get(0) + ".instance();";
    out += "\n" + pad + "   }";
    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   protected " + String.class.getName() + " _name() {";
    out += "\n" + pad + "     return _name;";
    out += "\n" + pad + "   }";
    if (properties != null) {
      for (final $Property property : properties)
        out += writeField(parents, property, depth);

      out += "\n\n" + pad + "   @" + Override.class.getName();
      out += "\n" + pad + "   protected " + String.class.getName() + " _encode(final int depth) {";
      out += "\n" + pad + "     final " + StringBuilder.class.getName() + " out = new " + StringBuilder.class.getName() + "(super._encode(depth));";
      out += "\n" + pad + "     final int startLength = out.length();";
      for (int i = 0; i < properties.size(); i++)
        out += writeEncode(properties.get(i), depth);

      out += "\n" + pad + "     return startLength == out.length() || startLength != 0 ? out.toString() : out.substring(2);\n" + pad + "   }";
    }

    if (!isAbstract) {
      out += "\n\n" + pad + "   @" + Override.class.getName();
      out += "\n" + pad + "   public " + className + " clone() {";
      out += "\n" + pad + "     return new " + className + "(this);";
      out += "\n" + pad + "   }";
    }

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   public boolean equals(final " + Object.class.getName() + " obj) {";
    out += "\n" + pad + "     if (obj == this)";
    out += "\n" + pad + "       return true;";
    out += "\n\n" + pad + "     if (!(obj instanceof " + className + ")" + (extendsPropertyName != null ? " || !super.equals(obj)" : "") + ")";
    out += "\n" + pad + "       return false;\n";
    if (properties != null) {
      out += "\n" + pad + "     final " + className + " that = (" + className + ")obj;";
      for (final $Property property : properties) {
        final String instanceName = getInstanceName(property);
        out += "\n" + pad + "     if (that." + instanceName + " != null ? !that." + instanceName + ".equals(" + instanceName + ") : " + instanceName + " != null)";
        out += "\n" + pad + "       return false;\n";
      }
    }
    out += "\n" + pad + "     return true;";
    out += "\n" + pad + "   }";

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   public int hashCode() {";
    if (properties != null) {
      out += "\n" + pad + "     int hashCode = " + className.hashCode() + (extendsPropertyName != null ? " ^ 31 * super.hashCode()" : "") + ";";
      for (final $Property property : properties) {
        final String instanceName = getInstanceName(property);
        out += "\n" + pad + "     if (" + instanceName + " != null)";
        out += "\n" + pad + "       hashCode ^= 31 * " + instanceName + ".hashCode();\n";
      }
      out += "\n" + pad + "     return hashCode;";
    }
    else {
      out += "\n" + pad + "     return " + className.hashCode() + (extendsPropertyName != null ? " ^ 31 * super.hashCode()" : "") + ";";
    }
    out += "\n" + pad + "   }";

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   public " + String.class.getName() + " toString() {";
    out += "\n" + pad + "     return encode(this, 1);";
    out += "\n" + pad + "   }";

    out += "\n" + pad + " }";

    parents.pop();
    return out.toString();
  }
}