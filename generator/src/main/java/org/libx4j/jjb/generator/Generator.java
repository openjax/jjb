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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.lib4j.jci.CompilationException;
import org.lib4j.jci.JavaCompiler;
import org.lib4j.lang.ClassLoaders;
import org.lib4j.lang.Resources;
import org.lib4j.lang.Strings;
import org.lib4j.util.Collections;
import org.lib4j.xml.XMLException;
import org.lib4j.xml.XMLText;
import org.lib4j.xml.dom.DOMStyle;
import org.lib4j.xml.dom.DOMs;
import org.libx4j.jjb.jsonx.xe.$jsonx_boolean;
import org.libx4j.jjb.jsonx.xe.$jsonx_element;
import org.libx4j.jjb.jsonx.xe.$jsonx_named;
import org.libx4j.jjb.jsonx.xe.$jsonx_number;
import org.libx4j.jjb.jsonx.xe.$jsonx_number._form$;
import org.libx4j.jjb.jsonx.xe.$jsonx_object;
import org.libx4j.jjb.jsonx.xe.$jsonx_property;
import org.libx4j.jjb.jsonx.xe.$jsonx_string;
import org.libx4j.jjb.jsonx.xe.jsonx_json;
import org.libx4j.jjb.runtime.Binding;
import org.libx4j.jjb.runtime.EncodeException;
import org.libx4j.jjb.runtime.JSArray;
import org.libx4j.jjb.runtime.JSBundle;
import org.libx4j.jjb.runtime.JSObject;
import org.libx4j.jjb.runtime.Property;
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

  public static void generate(final URL url, final File destDir, final boolean compile) throws GeneratorExecutionException, IOException, XMLException {
    final jsonx_json json = (jsonx_json)Bindings.parse(url);
    if (json._object() == null) {
      logger.error("Missing <object> elements: " + url.toExternalForm());
      return;
    }

    final String packageName = "jjb";
    final File outDir = new File(destDir, packageName.replace('.', '/'));
    if (!outDir.exists() && !outDir.mkdirs())
      throw new IOException("Unable to mkdirs: " + outDir.getAbsolutePath());

    for (final jsonx_json._object object : json._object())
      objectNameToObject.put(object._name$().text(), object);

    final String name = json._name$().text();

    String out = "";

    out += "package " + packageName + ";\n";
    if (!json._description(0).isNull())
      out += "\n/**\n * " + json._description(0).text() + "\n */";

    out += "\n@" + SuppressWarnings.class.getName() + "(\"all\")";
    out += "\npublic class " + name + " extends " + JSBundle.class.getName() + " {";
    out += "\n  public static final " + String.class.getName() + " mimeType = \"" + json._mimeType$().text() + "\";";
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
    for (final jsonx_json._object object : json._object())
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

      ClassLoaders.addURL((URLClassLoader)ClassLoader.getSystemClassLoader(), destDir.toURI().toURL());
    }
  }

  private static final Map<String,jsonx_json._object> objectNameToObject = new HashMap<String,jsonx_json._object>();

  private static String getType(final Stack<String> parent, final $jsonx_property property) {
    if (property instanceof $jsonx_string)
      return String.class.getName();

    if (property instanceof $jsonx_number) {
      final $jsonx_number numberProperty = ($jsonx_number)property;
      if ($jsonx_number._form$.integer.text().equals(numberProperty._form$().text()))
        return BigInteger.class.getName();

      if ($jsonx_number._form$.real.text().equals(numberProperty._form$().text()))
        return BigDecimal.class.getName();

      throw new UnsupportedOperationException("Unknown number form: " + numberProperty._form$().text());
    }

    if (property instanceof $jsonx_boolean)
      return Boolean.class.getName();

    if (property instanceof $jsonx_object) {
      final $jsonx_object object = ($jsonx_object)property;
      if (!object._extends$().isNull() && !property.elementIterator().hasNext())
        return Strings.toClassCase(object._extends$().text());

      return Collections.toString(parent, ".") + "." + Strings.toClassCase((($jsonx_object)property)._name$().text());
    }

    throw new UnsupportedOperationException("Unknown type: " + property.getClass().getName());
  }

  private static String getPropertyName(final $jsonx_property property) {
    if (property instanceof $jsonx_named)
      return (($jsonx_named)property)._name$().text();

    if (property instanceof $jsonx_object)
      return (($jsonx_object)property)._name$().text();

    throw new UnsupportedOperationException("Unsupported type: " + property);
  }

  private static String getInstanceName(final $jsonx_property property) {
    return Strings.toInstanceCase(getPropertyName(property));
  }

  private static String writeField(final Stack<String> parent, final $jsonx_property property, final int depth) {
    final String valueName = getPropertyName(property);
    final boolean isArray = property._array$().text();
    final String rawType = getType(parent, property);
    final String type = isArray ? List.class.getName() + "<" + rawType + ">" : rawType;

    final String instanceName = getInstanceName(property);

    final String pad = Strings.padFixed("", depth * 2, false);
    String out = "\n";
    if (!property._description(0).isNull())
      out += "\n" + pad + "   /**\n" + pad + "    * " + property._description(0).text() + "\n" + pad + "    */";

    out += "\n" + pad + "   public final " + Property.class.getName() + "<" + type + "> " + instanceName + " = new " + Property.class.getName() + "<" + type + ">(this, (" + Binding.class.getName() + "<" + type + ">)bindings.get(\"" + valueName + "\"));";
    return out;
  }

  private static String writeEncode(final $jsonx_property property, final int depth) {
    final String valueName = getPropertyName(property);
    final String instanceName = getInstanceName(property);
    final String pad = Strings.padFixed("", depth * 2, false);

    String out = "";
    if (property._required$().text()) {
      out += "\n" + pad + "     if (!" + instanceName + ".present())";
      out += "\n" + pad + "       throw new " + EncodeException.class.getName() + "(\"\\\"" + valueName + "\\\" is required\", this);\n";
    }

    if (!property._null$().text()) {
      out += "\n" + pad + "     if (" + instanceName + ".present() && " + instanceName + ".get() == null)";
      out += "\n" + pad + "       throw new " + EncodeException.class.getName() + "(\"\\\"" + valueName + "\\\" cannot be null\", this);\n";
    }

    out += "\n" + pad + "     if (" + instanceName + ".present() || required(" + instanceName + "))";
    out += "\n" + pad + "       out.append(\",\\n\").append(pad(depth)).append(\"\\\"" + valueName + "\\\": \").append(";
    if (property._array$().text())
      return out + JSArray.class.getName() + ".toString(encode(" + instanceName + "), depth + 1));\n";

    if (property instanceof $jsonx_object)
      return out + "" + instanceName + ".get() != null ? encode(encode(" + instanceName + "), depth + 1) : \"null\");\n";

    if (property instanceof $jsonx_string)
      return out + "" + instanceName + ".get() != null ? \"\\\"\" + encode(" + instanceName + ") + \"\\\"\" : \"null\");\n";

    return out + "encode(" + instanceName + "));\n";
  }

  private static String writeJavaClass(final Stack<String> parent, final $jsonx_element object, final int depth) throws GeneratorExecutionException {
    final String objectName;
    final String extendsPropertyName;
    final boolean skipUnknown;
    final boolean isAbstract;
    final BindingList<$jsonx_property> properties;
    if (object instanceof $jsonx_object) {
      final $jsonx_object object1 = ($jsonx_object)object;
      if (!object1._extends$().isNull() && !object.elementIterator().hasNext() && !objectNameToObject.get(object1._extends$().text())._abstract$().text())
        return "";

      objectName = object1._name$().text();
      extendsPropertyName = !object1._extends$().isNull() ? object1._extends$().text() : null;
      skipUnknown = $jsonx_object._onUnknown$.skip.text().equals(object1._onUnknown$().text());
      isAbstract = false;
      properties = object1._property();
    }
    else if (object instanceof jsonx_json._object) {
      final jsonx_json._object object2 = (jsonx_json._object)object;
      objectName = object2._name$().text();
      extendsPropertyName = !object2._extends$().isNull() ? object2._extends$().text() : null;
      skipUnknown = $jsonx_object._onUnknown$.skip.text().equals(object2._onUnknown$().text());
      isAbstract = object2._abstract$().text();
      properties = object2._property();
    }
    else {
      throw new UnsupportedOperationException("Unsupported object type: " + object.getClass().getName());
    }

    final String className = Strings.toClassCase(objectName);
    parent.add(className);

    final String pad = Strings.padFixed("", depth * 2, false);
    String out = "\n";
    if (!object._description(0).isNull())
      out += "\n" + pad + " /**\n" + pad + "  * " + object._description(0).text() + "\n" + pad + "  */";

    out += "\n" + pad + " public static" + (isAbstract ? " abstract" : "") + " class " + className + " extends " + (extendsPropertyName != null ? parent.get(0) + "." + Strings.toClassCase(extendsPropertyName) : JSObject.class.getName()) + " {";
    out += "\n" + pad + "   private static final " + String.class.getName() + " _name = \"" + objectName + "\";\n";
    out += "\n" + pad + "   private static final " + Map.class.getName() + "<" + String.class.getName() + "," + Binding.class.getName() + "<?>> bindings = new " + HashMap.class.getName() + "<" + String.class.getName() + "," + Binding.class.getName() + "<?>>(" + (properties != null ? properties.size() : 0) + ");";

    out += "\n" + pad + "   static {";
    out += "\n" + pad + "     registerBinding(_name, " + className + ".class);";
    if (properties != null) {
      out += "\n" + pad + "     try {";
      for (final $jsonx_property property : properties) {
        final String propertyName = getPropertyName(property);
        final String rawType = getType(parent, property);
        final boolean isArray = property._array$().text();
        final String type = isArray ? List.class.getName() + "<" + rawType + ">" : rawType;

        out += "\n" + pad + "       bindings.put(\"" + propertyName + "\", new " + Binding.class.getName() + "<" + type + ">(\"" + propertyName + "\", " + className + ".class.getDeclaredField(\"" + getInstanceName(property) + "\"), " + rawType + ".class, " + isAbstract + ", " + isArray + ", " + property._required$().text() + ", " + !property._null$().text() + (property instanceof $jsonx_string ? ", " + (($jsonx_string)property)._urlDecode$().text() + ", " + (($jsonx_string)property)._urlEncode$().text() : "");
        if (property instanceof $jsonx_string) {
          final $jsonx_string string = ($jsonx_string)property;
          if (string._pattern$().text() != null || string._length$().text() != null)
            out += ", new " + StringValidator.class.getName() + "(" + (string._pattern$().isNull() ? "null" : "\"" + XMLText.unescapeXMLText(string._pattern$().text()).replace("\\", "\\\\").replace("\"", "\\\"") + "\"") + ", " + (string._length$().isNull() ? "null" : string._length$().text()) + ")";
        }
        else if (property instanceof $jsonx_number) {
          final $jsonx_number string = ($jsonx_number)property;
          if (_form$.integer.text().equals(string._form$().text()) || string._min$().text() != null || string._max$().text() != null) {
            if (string._min$().text() != null && string._max$().text() != null && string._min$().text() > string._max$().text())
              throw new GeneratorExecutionException("min (" + string._min$().text() + ") > max (" + string._max$().text() + ") on property: " + objectName + "." + propertyName);

            out += ", new " + NumberValidator.class.getName() + "(" + _form$.integer.text().equals(string._form$().text()) + ", " + (string._min$().isNull() ? "null" : string._min$().text().intValue()) + ", " + (string._max$().isNull() ? "null" : string._max$().text().intValue()) + ")";
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
      for (final $jsonx_property property : properties)
        if (property instanceof $jsonx_object)
          out += writeJavaClass(parent, property, depth + 1);

    out += "\n\n" + pad + "   public " + className + "(final " + JSObject.class.getName() + " object) {";
    out += "\n" + pad + "     super(object);";
    if (properties != null) {
      out += "\n" + pad + "     if (!(object instanceof " + className + "))";
      out += "\n" + pad + "       return;";
      out += "\n\n" + pad + "     final " + className + " that = (" + className + ")object;";
      for (final $jsonx_property property : properties) {
        final String instanceName = getInstanceName(property);
        out += "\n" + pad + "     clone(this." + instanceName + ", that." + instanceName + ");";
      }
    }

    out += "\n" + pad + "   }";

    out += "\n\n" + pad + "   public " + className + "() {";
    out += "\n" + pad + "     super();";
    out += "\n" + pad + "   }";

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
    out += "\n" + pad + "     return " + parent.get(0) + ".instance();";
    out += "\n" + pad + "   }";
    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   protected " + String.class.getName() + " _name() {";
    out += "\n" + pad + "     return _name;";
    out += "\n" + pad + "   }";
    if (properties != null) {
      for (final $jsonx_property property : properties)
        out += writeField(parent, property, depth);

      out += "\n\n" + pad + "   @" + Override.class.getName();
      out += "\n" + pad + "   protected " + String.class.getName() + " _encode(final int depth) {";
      out += "\n" + pad + "     final " + StringBuilder.class.getName() + " out = new " + StringBuilder.class.getName() + "(super._encode(depth));";
      out += "\n" + pad + "     final int startLength = out.length();";
      for (int i = 0; i < properties.size(); i++)
        out += writeEncode(properties.get(i), depth);

      out += "\n" + pad + "     return startLength == out.length() || startLength != 0 ? out.toString() : out.substring(2);\n" + pad + "   }";
    }

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   public " + String.class.getName() + " toString() {";
    out += "\n" + pad + "     return encode(this, 1);";
    out += "\n" + pad + "   }";

    out += "\n\n" + pad + "   @" + Override.class.getName();
    out += "\n" + pad + "   public boolean equals(final " + Object.class.getName() + " obj) {";
    out += "\n" + pad + "     if (obj == this)";
    out += "\n" + pad + "       return true;";
    out += "\n\n" + pad + "     if (!(obj instanceof " + className + ")" + (extendsPropertyName != null ? " || !super.equals(obj)" : "") + ")";
    out += "\n" + pad + "       return false;\n";
    if (properties != null) {
      out += "\n" + pad + "     final " + className + " that = (" + className + ")obj;";
      for (final $jsonx_property property : properties) {
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
      for (final $jsonx_property property : properties) {
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

    out += "\n" + pad + " }";

    parent.pop();
    return out.toString();
  }
}