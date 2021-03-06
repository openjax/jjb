/* Copyright (c) 2015 OpenJAX
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

package org.openjax.jjb;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openjax.standard.maven.mojo.GeneratorMojo;
import org.openjax.standard.maven.mojo.SourceInput;
import org.openjax.standard.xml.api.ValidationException;
import org.openjax.jjb.generator.Generator;
import org.openjax.jjb.generator.GeneratorExecutionException;

@Mojo(name="generate", defaultPhase=LifecyclePhase.GENERATE_SOURCES)
@Execute(goal="generate")
public class JJBMojo extends GeneratorMojo {
  @SourceInput
  @Parameter(property="schemas", required=true)
  private List<String> schemas;

  @Override
  public void execute(final Configuration configuration) throws MojoExecutionException, MojoFailureException {
    try {
      for (final URL schema : configuration.getSourceInputs("schemas"))
        Generator.generate(schema, configuration.getDestDir(), false);
    }
    catch (final GeneratorExecutionException | IOException | ValidationException e) {
      throw new MojoExecutionException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    }
  }
}