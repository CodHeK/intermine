package org.intermine.web.results;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.intermine.model.InterMineObject;
import org.intermine.metadata.PrimaryKeyUtil;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.metadata.CollectionDescriptor;
import org.intermine.metadata.Model;
import org.intermine.objectstore.proxy.LazyCollection;

import org.intermine.util.TypeUtil;

/**
 * Class to represent an object for display in the webapp
 * @author Mark Woodbridge
 */
public class DisplayObject
{
    InterMineObject object;
    Set clds;
    Map keyAttributes = new HashMap();
    Map keyReferences = new HashMap();
    Map attributes = new HashMap();
    Map references = new HashMap();
    Map collections = new HashMap();
    
    /**
     * Constructor
     * @param object the object to display
     * @param model the metadata for the object
     * @throws Exception if an error occurs
     */
    public DisplayObject(InterMineObject object, Model model) throws Exception {
        this.object = object;
        clds = ObjectViewController.getLeafClds(object.getClass(), model);

        for (Iterator i = PrimaryKeyUtil.getPrimaryKeyFields(model, object.getClass()).iterator();
             i.hasNext();) {
            FieldDescriptor fd = (FieldDescriptor) i.next();
            Object fieldValue = TypeUtil.getFieldValue(object, fd.getName());
            if (fieldValue != null) {
                if (fd.isAttribute() && !fd.getName().equals("id")) {
                    keyAttributes.put(fd.getName(), fieldValue);
                } else if (fd.isReference()) {
                    keyReferences.put(fd.getName(),
                                      new DisplayReference((InterMineObject) fieldValue, model));
                }
            }
        }

        for (Iterator i = clds.iterator(); i.hasNext();) {
            ClassDescriptor cld = (ClassDescriptor) i.next();
            for (Iterator j = cld.getAllFieldDescriptors().iterator(); j.hasNext();) {
                FieldDescriptor fd = (FieldDescriptor) j.next();
                Object fieldValue = TypeUtil.getFieldValue(object, fd.getName());
                if (fieldValue != null) {
                    if (fd.isAttribute() && !fd.getName().equals("id")) {
                        attributes.put(fd.getName(), fieldValue);
                    } else if (fd.isReference()) {
                        references.put(fd.getName(),
                                       new DisplayReference((InterMineObject) fieldValue, model));
                    } else if (fd.isCollection()) {
                        ClassDescriptor refCld =
                            ((CollectionDescriptor) fd).getReferencedClassDescriptor();
                        DisplayCollection collection =
                            new DisplayCollection((LazyCollection) fieldValue, refCld);
                        if (collection.getSize() > 0) {
                            collections.put(fd.getName(), collection);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Get the real business object
     * @return the object
     */
    public InterMineObject getObject() {
        return object;
    }
    
    /**
     * Get the id of this object
     * @return the id
     */
    public int getId() {
        return object.getId().intValue();
    }
    
    /**
     * Get the class descriptors for this object
     * @return the class descriptors
     */
    public Set getClds() {
        return clds;
    }

    /**
     * Get the key attribute fields and values for this object
     * @return the key attributes
     */
    public Map getKeyAttributes() {
        return keyAttributes;
    }

    /**
     * Get the key reference fields and values for this object
     * @return the key references
     */
    public Map getKeyReferences() {
        return keyReferences;
    }

    /**
     * Get the attribute fields and values for this object
     * @return the attributes
     */
    public Map getAttributes() {
        return attributes;
    }

    /**
     * Get the reference fields and values for this object
     * @return the references
     */
    public Map getReferences() {
        return references;
    }

    /**
     * Get the collection fields and values for this object
     * @return the collections
     */
    public Map getCollections() {
        return collections;
    }
}
