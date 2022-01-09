/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.core.mapping;

import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchDateConverter;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

/**
 * Elasticsearch specific {@link org.springframework.data.mapping.PersistentProperty} implementation processing
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Mark Paluch
 * @author Sascha Woo
 * @author Oliver Gierke
 * @author Peter-Josef Meisch
 */
public class SimpleElasticsearchPersistentProperty extends
    AnnotationBasedPersistentProperty<ElasticsearchPersistentProperty> implements ElasticsearchPersistentProperty {

    @Nullable
    private ElasticsearchPersistentPropertyConverter propertyConverter;

    private static final List<String> SUPPORTED_ID_PROPERTY_NAMES = Arrays.asList("id");

    private final boolean isScore;
    private final boolean isParent;
    private final boolean isId;
    private final @org.springframework.lang.Nullable String annotatedFieldName;

    public SimpleElasticsearchPersistentProperty(Property property,
                                                 PersistentEntity<?, ElasticsearchPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {

        super(property, owner, simpleTypeHolder);

        this.isId = super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getFieldName());
        this.isScore = isAnnotationPresent(Score.class);
        this.isParent = isAnnotationPresent(Parent.class);
        this.annotatedFieldName = getAnnotatedFieldName();

        if (isVersionProperty() && !getType().equals(Long.class)) {
            throw new MappingException(String.format("Version property %s must be of type Long!", property.getName()));
        }

        if (isScore && !getType().equals(Float.TYPE) && !getType().equals(Float.class)) {
            throw new MappingException(
                String.format("Score property %s must be either of type float or Float!", property.getName()));
        }

        if (isParent && !getType().equals(String.class)) {
            throw new MappingException(String.format("Parent property %s must be of type String!", property.getName()));
        }
        initDateConverter();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#getFieldName()
     */
    @Override
    public String getFieldName() {
        return annotatedFieldName == null ? getProperty().getName() : annotatedFieldName;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty#isIdProperty()
     */
    @Override
    public boolean isIdProperty() {
        return isId;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AbstractPersistentProperty#createAssociation()
     */
    @Override
    protected Association<ElasticsearchPersistentProperty> createAssociation() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#isScoreProperty()
     */
    @Override
    public boolean isScoreProperty() {
        return isScore;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AbstractPersistentProperty#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return false;
    }

    @org.springframework.lang.Nullable
    private String getAnnotatedFieldName() {

        if (isAnnotationPresent(Field.class)) {

            String name = findAnnotation(Field.class).name();
            return StringUtils.hasText(name) ? name : null;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#isParentProperty()
     */
    @Override
    public boolean isParentProperty() {
        return isParent;
    }

    @Override
    public boolean hasPropertyConverter() {
        return propertyConverter != null;
    }

    @Override
    public ElasticsearchPersistentPropertyConverter getPropertyConverter() {
        //return super(getPropertyConverter());
        return propertyConverter;
    }

    /**
     * Initializes an {@link ElasticsearchPersistentPropertyConverter} if this property is annotated as a Field with type
     * {@link FieldType#Date}, has a {@link DateFormat} set and if the type of the property is one of the Java8 temporal
     * classes.
     */
    private void initDateConverter() {
        Field field = findAnnotation(Field.class);
        if (field != null && field.type() == FieldType.Date && TemporalAccessor.class.isAssignableFrom(getType())) {
            DateFormat dateFormat = field.format();
            ElasticsearchDateConverter converter = null;
            if (dateFormat == DateFormat.custom) {
                String pattern = field.pattern();
                if (StringUtils.hasLength(pattern)) {
                    converter = ElasticsearchDateConverter.of(pattern);
                }
            } else if (dateFormat != DateFormat.none) {
                converter = ElasticsearchDateConverter.of(dateFormat);
            }
            if (converter != null) {
                ElasticsearchDateConverter dateConverter = converter;
                propertyConverter = new ElasticsearchPersistentPropertyConverter() {
                    @Override
                    public String write(Object property) {
                        return dateConverter.format((TemporalAccessor) property);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Object read(String s) {
                        return dateConverter.parse(s, (Class<? extends TemporalAccessor>) getType());
                    }
                };
            }
        }
    }
}
