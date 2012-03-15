package com.simbiosys.apps.foodfast.utils;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class SimpleMenu implements Menu {

	private Context context;
	private Resources resources;

	private ArrayList<SimpleMenuItem> menuItems;

	protected SimpleMenu(Context context) {
		super();
		this.context = context;
		resources = context.getResources();
		menuItems = new ArrayList<SimpleMenuItem>();
	}

	public Context getContext() {
		return context;
	}

	public Resources getResources() {
		return resources;
	}

	@Override
	public MenuItem add(CharSequence title) {
		return addInternal(0, 0, title);
	}

	@Override
	public MenuItem add(int titleRes) {
		return addInternal(0, 0, resources.getString(titleRes));
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		return addInternal(itemId, order, title);
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, int titleRes) {
		return addInternal(itemId, order, resources.getString(titleRes));
	}

	/**
	 * Adds an item to the menu. All other add methods funnel to this.
	 */
	private MenuItem addInternal(int itemId, int order, CharSequence title) {
		final SimpleMenuItem item = new SimpleMenuItem(this, itemId, order, title);
		menuItems.add(findInsertIndex(menuItems, order), item);
		return item;
	}

	private static int findInsertIndex(ArrayList<? extends MenuItem> items, int order) {
		for (int i = items.size() - 1; i >= 0; i--) {
			MenuItem item = items.get(i);
			if (item.getOrder() <= order) {
				return i + 1;
			}
		}
		return 0;
	}
	
	@Override
	public MenuItem findItem(int id) {
		final int size = size();
		for (int i = 0; i < size; i++) {
			SimpleMenuItem item = menuItems.get(i);
			if (item.getItemId() == id) {
				return item;
			}
		}
		return null;
	}

	public int findItemIndex(int id) {
		final int size = size();
		for (int i = 0; i < size; i++) {
			SimpleMenuItem item = menuItems.get(i);
			if (item.getItemId() == id) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void removeItem(int id) {
		removeItemAtIndex(findItemIndex(id));
	}

	private void removeItemAtIndex(int index) {
		if (index < 0 || index >= menuItems.size()) {
			return;
		}
		menuItems.remove(index);
	}

	@Override
	public void clear() {
		menuItems.clear();
	}

	@Override
	public int size() {
		return menuItems.size();
	}

	@Override
	public MenuItem getItem(int index) {
		return menuItems.get(index);
	}

	// Unsupported Operations

	@Override
	public SubMenu addSubMenu(CharSequence title) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public SubMenu addSubMenu(int titleRes) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller,
			Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public void removeGroup(int groupId) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public void setGroupVisible(int group, boolean visible) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public void setGroupEnabled(int group, boolean enabled) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public boolean hasVisibleItems() {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public boolean isShortcutKey(int keyCode, KeyEvent event) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public boolean performIdentifierAction(int id, int flags) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

	@Override
	public void setQwertyMode(boolean isQwerty) {
		throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
	}

}
