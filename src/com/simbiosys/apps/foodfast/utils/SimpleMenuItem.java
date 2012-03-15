package com.simbiosys.apps.foodfast.utils;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

public class SimpleMenuItem implements MenuItem {

	private final int id;
	private final int order;
	private SimpleMenu menu;
	private CharSequence title;
	private CharSequence titleCondensed;
	private Drawable iconDrawable;
	private int iconResId = 0;
	private boolean enabled = true;

	protected SimpleMenuItem(SimpleMenu menu, int id, int order, CharSequence title) {
		this.menu = menu;
		this.id = id;
		this.order = order;
		this.title = title;
	}

	@Override
	public int getItemId() {
		return id;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public MenuItem setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

	@Override
	public MenuItem setTitle(int titleRes) {
		return setTitle(menu.getContext().getString(titleRes));
	}

	@Override
	public CharSequence getTitle() {
		return title;
	}

	@Override
	public MenuItem setTitleCondensed(CharSequence title) {
		titleCondensed = title;
		return this;
	}

	@Override
	public CharSequence getTitleCondensed() {
		return titleCondensed != null ? titleCondensed : title;
	}

	@Override
	public MenuItem setIcon(Drawable icon) {
		iconResId = 0;
		iconDrawable = icon;
		return this;
	}

	@Override
	public MenuItem setIcon(int iconRes) {
		iconResId = iconRes;
		iconDrawable = null;
		return this;
	}

	@Override
	public Drawable getIcon() {
		if (iconDrawable != null) {
			return iconDrawable;
		}

		if (iconResId != 0) {
			return menu.getResources().getDrawable(iconResId);
		}

		return null;
	}
	
	@Override
	public MenuItem setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	// No-op operations.

	@Override
	public int getGroupId() {
		return 0;
	}

	@Override
	public MenuItem setIntent(Intent intent) {
		return this;
	}

	@Override
	public Intent getIntent() {
		return null;
	}

	@Override
	public MenuItem setShortcut(char numericChar, char alphaChar) {
		return this;
	}

	@Override
	public MenuItem setNumericShortcut(char numericChar) {
		return this;
	}

	@Override
	public char getNumericShortcut() {
		return 0;
	}

	@Override
	public MenuItem setAlphabeticShortcut(char alphaChar) {
		return this;
	}

	@Override
	public char getAlphabeticShortcut() {
		return 0;
	}

	@Override
	public MenuItem setCheckable(boolean checkable) {
		return this;
	}

	@Override
	public boolean isCheckable() {
		return false;
	}

	@Override
	public MenuItem setChecked(boolean checked) {
		return this;
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@Override
	public MenuItem setVisible(boolean visible) {
		return this;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean hasSubMenu() {
		return false;
	}

	@Override
	public SubMenu getSubMenu() {
		return null;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
		return this;
	}

	@Override
	public ContextMenuInfo getMenuInfo() {
		return null;
	}

	@Override
	public void setShowAsAction(int actionEnum) {

	}

	@Override
	public MenuItem setShowAsActionFlags(int actionEnum) {
		return null;
	}

	@Override
	public MenuItem setActionView(View view) {
		return this;
	}

	@Override
	public MenuItem setActionView(int resId) {
		return this;
	}

	@Override
	public View getActionView() {
		return null;
	}

	@Override
	public MenuItem setActionProvider(ActionProvider actionProvider) {
		return this;
	}

	@Override
	public ActionProvider getActionProvider() {
		return null;
	}

	@Override
	public boolean expandActionView() {
		return false;
	}

	@Override
	public boolean collapseActionView() {
		return false;
	}

	@Override
	public boolean isActionViewExpanded() {
		return false;
	}

	@Override
	public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
		return this;
	}

}
