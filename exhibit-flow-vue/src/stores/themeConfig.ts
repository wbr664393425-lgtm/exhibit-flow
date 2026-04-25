import { defineStore } from 'pinia';

/**
 * 修改配置时：
 * 1、需要每次都清理 `window.localStorage` 浏览器永久缓存
 * 2、或者点击布局配置最底部 `一键恢复默认` 按钮即可看到效果
 */
export const useThemeConfig = defineStore('themeConfig', {
	state: (): ThemeConfigState => ({
		themeConfig:{"isDrawer":false,"primary":"#111111","isIsDark":false,"topBar":"#ffffff","topBarColor":null,"isTopBarColorGradual":false,"menuBar":"#111111","menuBarColor":"#edebe4","menuBarActiveColor":"rgba(255, 255, 255, 0.1)","isMenuBarColorGradual":false,"columnsMenuBar":"#545c64","columnsMenuBarColor":"#e6e6e6","isColumnsMenuBarColorGradual":false,"isColumnsMenuHoverPreload":false,"isCollapse":false,"isUniqueOpened":true,"isFixedHeader":false,"isFixedHeaderChange":false,"isClassicSplitMenu":false,"isLockScreen":false,"lockScreenTime":30,"isShowLogo":true,"isShowLogoChange":false,"isBreadcrumb":true,"isTagsview":true,"isBreadcrumbIcon":false,"isTagsviewIcon":false,"isCacheTagsView":true,"isSortableTagsView":true,"isShareTagsView":false,"isFooter":true,"isGrayscale":false,"isInvert":false,"isWartermark":false,"wartermarkText":"后台管理系统","tagsStyle":"tags-style-one","animation":"slide-right","columnsAsideStyle":"columns-round","columnsAsideLayout":"columns-vertical","layout":"defaults","isRequestRoutes":true,"globalTitle":"后台管理系统","globalViceTitle":"后台管理系统","globalViceTitleMsg":"专业、安全、高效、监管、服务","globalI18n":"zh-cn","globalComponentSize":"default","footerAuthor":"©2025 后台管理系统"} 
		// { "isDrawer": false, "primary": "#2E5CF6", "isIsDark": false, "topBar": "#ffffff", "topBarColor": "#606266", "isTopBarColorGradual": false, "menuBar": "#FFFFFF", "menuBarColor": "#505968", "menuBarActiveColor": "rgba(242, 243, 245, 1)", "isMenuBarColorGradual": false, "columnsMenuBar": "#545c64", "columnsMenuBarColor": "#e6e6e6", "isColumnsMenuBarColorGradual": false, "isColumnsMenuHoverPreload": false, "isCollapse": false, "isUniqueOpened": true, "isFixedHeader": false, "isFixedHeaderChange": false, "isClassicSplitMenu": false, "isLockScreen": false, "lockScreenTime": 30, "isShowLogo": true, "isShowLogoChange": false, "isBreadcrumb": true, "isTagsview": true, "isBreadcrumbIcon": false, "isTagsviewIcon": false, "isCacheTagsView": true, "isSortableTagsView": true, "isShareTagsView": false, "isFooter": true, "isGrayscale": false, "isInvert": false, "isWartermark": false, "wartermarkText": "后台管理系统", "tagsStyle": "tags-style-five", "animation": "slide-right", "columnsAsideStyle": "columns-round", "columnsAsideLayout": "columns-vertical", "layout": "transverse", "isRequestRoutes": true, "globalTitle": "后台管理系统", "globalViceTitle": "后台管理系统", "globalViceTitleMsg": "专业、安全、高效、监管、服务", "globalI18n": "zh-cn", "globalComponentSize": "default", "footerAuthor": "©2025 后台管理系统" },
	}),
	actions: {
		setThemeConfig(data: ThemeConfigState) {
			this.themeConfig = data.themeConfig;
		},
	},
});
