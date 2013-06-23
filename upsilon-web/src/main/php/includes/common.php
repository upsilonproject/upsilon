<?php

date_default_timezone_set('Europe/London');

set_include_path(get_include_path() . PATH_SEPARATOR . 'includes/libraries/');

require_once 'includes/functions.php';

require_once 'libAllure/ErrorHandler.php';
require_once 'libAllure/Session.php';
require_once 'libAllure/AuthBackendDatabase.php';
require_once 'libAllure/FormHandler.php';
require_once 'libAllure/HtmlLinksCollection.php';

use \libAllure\ErrorHandler;
use \libAllure\Session;
use \libAllure\AuthBackend;
use \libAllure\AuthBackendDatabase;

ErrorHandler::getInstance()->beGreedy();

require_once 'libAllure/Database.php';
require_once 'libAllure/Template.php';

use \libAllure\Template;

$tpl = new Template('upsilonGui');

use \libAllure\Database;
use \libAllure\DatabaseFactory;

if ((@include 'includes/config.php') !== false) {
	require_once 'includes/config.php';

	$db = connectDatabase();

	$backend = new AuthBackendDatabase();
	$backend->setSalt(null, CFG_PASSWORD_SALT);
	$backend->registerAsDefault();

	Session::start();

	if (!Session::isLoggedIn()) {
		require_once 'login.php';
	}
} else if (!defined('INSTALLATION_IN_PROGRESS')) {
	redirect('installer.php', 'No config file found. Assuming installation.');
}

?>