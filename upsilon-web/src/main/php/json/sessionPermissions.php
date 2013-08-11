<?php

require_once 'jsonCommon.php';

use \libAllure\Session;

outputJson(array(
	'viewDashboard' => Session::hasPriv('VIEW_DASHBOARD'),
	'superuser' => Session::hasPriv('SUPERUSER'),
));

?>