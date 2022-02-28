/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ohos.devtools.views.common.customcomp;

import org.apache.commons.lang.StringUtils;

import javax.swing.AbstractListModel;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * CustomListFilerModel
 *
 * @since 2021/5/19 16:39
 */
public class CustomListFilerModel<E> extends AbstractListModel<E> {
    private Vector<E> items = new Vector<E>();
    private Vector<E> filterItems = new Vector<E>();
    private boolean defaultFilterItems = true;

    /**
     * getSize
     *
     * @return int
     */
    public int getSize() {
        return defaultFilterItems ? items.size() : filterItems.size();
    }

    /**
     * getElementAt
     *
     * @param index index
     * @return E
     */
    public E getElementAt(int index) {
        return defaultFilterItems ? items.elementAt(index) : filterItems.elementAt(index);
    }

    /**
     * size
     *
     * @return int
     */
    public int size() {
        return defaultFilterItems ? items.size() : filterItems.size();
    }

    /**
     * isEmpty
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return defaultFilterItems ? items.isEmpty() : filterItems.isEmpty();
    }

    /**
     * elements
     *
     * @return Enumeration <E>
     */
    public Enumeration<E> elements() {
        return items.elements();
    }

    /**
     * contains
     *
     * @param elem elem
     * @return boolean
     */
    public boolean contains(Object elem) {
        return items.contains(elem);
    }

    /**
     * indexOf
     *
     * @param elem elem
     * @return int
     */
    public int indexOf(Object elem) {
        return items.indexOf(elem);
    }

    /**
     * indexOf
     *
     * @param elem elem
     * @param index index
     * @return int
     */
    public int indexOf(Object elem, int index) {
        return items.indexOf(elem, index);
    }

    /**
     * lastIndexOf
     *
     * @param elem elem
     * @return int
     */
    public int lastIndexOf(Object elem) {
        return items.lastIndexOf(elem);
    }

    /**
     * lastIndexOf
     *
     * @param elem elem
     * @param index index
     * @return int
     */
    public int lastIndexOf(Object elem, int index) {
        return items.lastIndexOf(elem, index);
    }

    /**
     * elementAt
     *
     * @param index index
     * @return E
     */
    public E elementAt(int index) {
        return items.elementAt(index);
    }

    /**
     * firstElement
     *
     * @return E
     */
    public E firstElement() {
        return items.firstElement();
    }

    /**
     * lastElement
     *
     * @return E
     */
    public E lastElement() {
        return items.lastElement();
    }

    /**
     * setElementAt
     *
     * @param element element
     * @param index index
     */
    public void setElementAt(E element, int index) {
        items.setElementAt(element, index);
        fireContentsChanged(this, index, index);
    }

    /**
     * removeElementAt
     *
     * @param index index
     */
    public void removeElementAt(int index) {
        items.removeElementAt(index);
        fireIntervalRemoved(this, index, index);
    }

    /**
     * insertElementAt
     *
     * @param element element
     * @param index index
     */
    public void insertElementAt(E element, int index) {
        items.insertElementAt(element, index);
        fireIntervalAdded(this, index, index);
    }

    /**
     * addElement
     *
     * @param element element
     */
    public void addElement(E element) {
        int index = items.size();
        items.addElement(element);
        fireIntervalAdded(this, index, index);
    }

    /**
     * removeElement
     *
     * @param obj obj
     * @return boolean
     */
    public boolean removeElement(Object obj) {
        int index = indexOf(obj);
        boolean rv = items.removeElement(obj);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return rv;
    }

    /**
     * removeAllElements
     */
    public void removeAllElements() {
        int index1 = items.size() - 1;
        items.removeAllElements();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    /**
     * toString
     *
     * @return String
     */
    public String toString() {
        return items.toString();
    }

    /**
     * toArray
     *
     * @return Object[]
     */
    public Object[] toArray() {
        Object[] rv = new Object[items.size()];
        items.copyInto(rv);
        return rv;
    }

    /**
     * get
     *
     * @param index index
     * @return E
     */
    public E get(int index) {
        return items.elementAt(index);
    }

    /**
     * clear
     */
    public void clear() {
        int index1;
        if (defaultFilterItems) {
            index1 = filterItems.size() - 1;
        } else {
            index1 = items.size() - 1;
        }
        items.removeAllElements();
        filterItems.removeAllElements();
        defaultFilterItems = true;
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    /**
     * addAll
     *
     * @param collection collection
     */
    public void addAll(Collection<? extends E> collection) {
        if (collection.isEmpty()) {
            return;
        }
        int startIndex = getSize();
        items.addAll(collection);
        fireIntervalAdded(this, startIndex, getSize() - 1);
    }

    /**
     * addAll
     *
     * @param index index
     * @param collection collection
     */
    public void addAll(int index, Collection<? extends E> collection) {
        if (index < 0 || index > getSize()) {
            throw new ArrayIndexOutOfBoundsException("index out of range: " + index);
        }
        if (collection.isEmpty()) {
            return;
        }
        items.addAll(index, collection);
        fireIntervalAdded(this, index, index + collection.size() - 1);
    }

    /**
     * refilter
     *
     * @param text text
     */
    public void refilter(String text) {
        if (StringUtils.isBlank(text)) {
            defaultFilterItems = true;
        } else {
            defaultFilterItems = false;
            filterItems.clear();
            String term = text;
            for (int index = 0; index < items.size(); index++) {
                if (items.get(index).toString().indexOf(term, 0) != -1) {
                    filterItems.add(items.get(index));
                }
            }
        }
        fireContentsChanged(this, 0, getSize());
    }
}
