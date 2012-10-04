/*
 * Copyright (C) 2008-2012 Open Wide SA
 *  
 * This library is free software; you can redistribute 
 * it and/or modify it under the terms of version 2.1 of 
 * the GNU Lesser General Public License as published by  
 * the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General 
 * Public License along with this library; if not, write to the 
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 * Boston, MA  02111-1307  USA
 * 
 * More information at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector/
 */

package fr.openwide.talendalfresco.acpxml;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Not synchronized, unlike Stack (Vector based).
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 * @param <T>
 */
public class ArrayListStack<T> extends ArrayList<T> {
   private static final long serialVersionUID = -7269326821799838046L;

   public ArrayListStack() {
      super();
   }

   public ArrayListStack(Collection<? extends T> c) {
      super(c);
   }

   public ArrayListStack(int initialCapacity) {
      super(initialCapacity);
   }

   public void push(T o) {
      this.add(o);
   }

   public T pop() {
      return this.remove(this.size() - 1);
   }
   
}
