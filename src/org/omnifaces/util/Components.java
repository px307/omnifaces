/*
 * Copyright 2012 OmniFaces.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.omnifaces.util;

import static org.omnifaces.util.Utils.isEmpty;

import javax.el.ValueExpression;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;


/**
 * Collection of utility methods for the JSF API that are mainly shortcuts for obtaining stuff from the
 * {@link UIComponent}.
 *
 * @author Bauke Scholtz
 * @author Arjan Tijms
 */
public final class Components {

	// Constructors ---------------------------------------------------------------------------------------------------

	private Components() {
		// Hide constructor.
	}

	// Utility --------------------------------------------------------------------------------------------------------

	/**
	 * Returns the current UI component from the EL context.
	 * @return The current UI component from the EL context.
	 * @see UIComponent#getCurrentComponent(FacesContext)
	 */
	public static UIComponent getCurrentComponent() {
		return UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
	}

	/**
	 * Returns the UI component matching the given client ID search expression.
	 * @param clientId The client ID search expression.
	 * @return The UI component matching the given client ID search expression.
	 * @throws ClassCastException When <code>T</code> is of wrong type.
	 * @see UIComponent#findComponent(String)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends UIComponent> T findComponent(String clientId) {
		return (T) FacesContext.getCurrentInstance().getViewRoot().findComponent(clientId);
	}
	
	
	/**
	 * Returns the UI component matching the given client ID search expression relative to the point
	 * in the component tree of the given component. For this search both parents and children are
	 * consulted, increasingly moving further away from the given component. Parents are consulted
	 * first, then children.
	 * 
	 * @param component the component from which the relative search is started.
	 * @param clientId The client ID search expression.
	 * @return The UI component matching the given client ID search expression.
	 * @throws ClassCastException When <code>T</code> is of wrong type.
	 * @see UIComponent#findComponent(String)
	 */
	public static <T extends UIComponent> T findComponentRelatively(UIComponent component, String clientId) {
		
		if (isEmpty(clientId)) {
			return null;
		}
		
		// Search first in the naming container parents of the given component
		T result = findComponentInParents(component, clientId);
		if (result == null) {
			// If not in the parents, search from the root
			result = findComponentInChildren(Faces.getViewRoot(), clientId);
		}
		
		return result;		
	}

	/**
	 * Returns the UI component matching the given client ID search expression relative to the point
	 * in the component tree of the given component, searching only in its parents.
	 * 
	 * @param component the component from which the relative search is started.
	 * @param clientId The client ID search expression.
	 * @return The UI component matching the given client ID search expression.
	 * @throws ClassCastException When <code>T</code> is of wrong type.
	 * @see UIComponent#findComponent(String)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends UIComponent> T findComponentInParents(UIComponent component, String clientId) {
		
		if (isEmpty(clientId)) {
			return null;
		}

		UIComponent parent = component;
		while (parent != null) {
			
			UIComponent result = null;
			if (parent instanceof NamingContainer) {
				try {
					result = parent.findComponent(clientId);
				} catch (IllegalArgumentException e) {
					continue;
				}
			}
			
			
			if (result != null) {
				return (T) result;
			}
			
			parent = parent.getParent();
		}

		return null;
	}
		
	/**
	 * Returns the UI component matching the given client ID search expression relative to the point
	 * in the component tree of the given component, searching only in its children.
	 * 
	 * @param component the component from which the relative search is started.
	 * @param clientId The client ID search expression.
	 * @return The UI component matching the given client ID search expression.
	 * @throws ClassCastException When <code>T</code> is of wrong type.
	 * @see UIComponent#findComponent(String)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends UIComponent> T findComponentInChildren(UIComponent component, String clientId) {
		
		if (isEmpty(clientId)) {
			return null;
		}

		for (UIComponent child : component.getChildren()) {

			UIComponent result = null;
			if (child instanceof NamingContainer) {
				try {
					result = child.findComponent(clientId);
				} catch (IllegalArgumentException e) {
					continue;
				}
			}

			if (result == null) {
				result = findComponentInChildren(child, clientId);
			}

			if (result != null) {
				return (T) result;
			}
		}

		return null;
	}
	
	
	
	/**
	 * Returns from the given component the closest parent of the given parent type, or <code>null</code> if none
	 * is found.
	 * @param <T> The generic parent type.
	 * @param component The component to return the closest parent of the given parent type for.
	 * @param parentType The parent type.
	 * @return From the given component the closest parent of the given parent type, or <code>null</code> if none
	 * is found.
	 */
	public static <T extends UIComponent> T getClosestParent(UIComponent component, Class<T> parentType) {
		UIComponent parent = component.getParent();

		while (parent != null && !parentType.isInstance(parent)) {
			parent = parent.getParent();
		}

		return parentType.cast(parent);
	}

	/**
	 * Returns whether the given UI input component is editable. That is when it is rendered, not disabled and not
	 * readonly.
	 * @param input The UI input component to be checked.
	 * @return <code>true</code> if the given UI input component is editable.
	 */
	public static boolean isEditable(UIInput input) {
		return input.isRendered()
			&& !Boolean.TRUE.equals(input.getAttributes().get("disabled"))
			&& !Boolean.TRUE.equals(input.getAttributes().get("readonly"));
	}

	/**
	 * Returns the value of the <code>label</code> attribute associated with the given UI component if any, else
	 * the client ID. It never returns null.
	 * @param input The UI input component for which the label is to be retrieved.
	 * @return The value of the <code>label</code> attribute associated with the given UI component if any, else
	 * the client ID.
	 */
	public static String getLabel(UIComponent input) {
		Object label = getOptionalLabel(input);
		return (label != null) ? label.toString() : input.getClientId();
	}
	
	/**
	 * Returns the value of the <code>label</code> attribute associated with the given UI component if any, else
	 * null.
	 * @param input The UI input component for which the label is to be retrieved.
	 * @return The value of the <code>label</code> attribute associated with the given UI component if any, else
	 * null.
	 */
	public static Object getOptionalLabel(UIComponent input) {
		Object label = input.getAttributes().get("label");
		if (label == null || (label instanceof String && ((String) label).length() == 0)) {
			ValueExpression labelExpression = input.getValueExpression("label");
			if (labelExpression != null) {
				label = labelExpression.getValue(FacesContext.getCurrentInstance().getELContext());
			}
		}
		
		return label; 
	}

	/**
	 * Returns the value of the given editable value holder component without the need to know if the given component
	 * has already been converted/validated or not.
	 * @param component The editable value holder component to obtain the value for.
	 * @return The value of the given editable value holder component.
	 */
	public static Object getValue(EditableValueHolder component) {
		Object submittedValue = component.getSubmittedValue();
		return (submittedValue != null) ? submittedValue : component.getLocalValue();
	}

	/**
	 * Returns whether the given editable value holder component has a submitted value.
	 * @param component The editable value holder component to be checked.
	 * @return <code>true</code> if the given editable value holder component has a submitted value, otherwise
	 * <code>false</code>.
	 */
	public static boolean hasSubmittedValue(EditableValueHolder component) {
		Object submittedValue = component.getSubmittedValue();
		return (submittedValue != null && !String.valueOf(submittedValue).isEmpty());
	}

}