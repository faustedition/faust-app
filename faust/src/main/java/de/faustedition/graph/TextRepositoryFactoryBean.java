/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.graph;

import eu.interedition.text.TextRepository;
import eu.interedition.text.json.JacksonDataNodeMapper;
import eu.interedition.text.neo4j.Neo4jTextRepository;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class TextRepositoryFactoryBean extends AbstractFactoryBean<Neo4jTextRepository<JsonNode>> {

  @Autowired
  private FaustGraph faustGraph;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Class<?> getObjectType() {
    return Neo4jTextRepository.class;
  }

  @Override
  protected Neo4jTextRepository<JsonNode> createInstance() throws Exception {
    return new Neo4jTextRepository<JsonNode>(JsonNode.class, new JacksonDataNodeMapper<JsonNode>(objectMapper), faustGraph.getDb(), false);
  }
}
