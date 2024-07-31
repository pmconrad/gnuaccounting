<?php
/*
  Copyright (c) 2010 Gerd Bartelt
  Version 0.2 by Jochen Staerk
  Date: 2011/06/18

  Released under the GNU General Public License
 */

// Set the level of error reporting
error_reporting(E_ALL & ~E_NOTICE);

// check support for register_globals
if (function_exists('ini_get') && (ini_get('register_globals') == false) && (PHP_VERSION < 4.3) ) {
	exit('Server Requirement Error: register_globals is disabled in your PHP configuration. This can be enabled in your php.ini configuration file or in the .htaccess file in your catalog directory. Please use PHP 4.3+ if register_globals cannot be enabled on the server.');
}

// Set the local configuration parameters - mainly for developers
if (file_exists('includes/local/configure.php')) include('includes/local/configure.php');

// Include application configuration parameters
require('includes/configure.php');

// Define the project version
define('PROJECT_VERSION', 'osCommerce Online Merchant v2.2 RC2a');

// some code to solve compatibility issues
require(DIR_WS_FUNCTIONS . 'compatibility.php');

// set php_self in the local scope
$PHP_SELF = (isset($HTTP_SERVER_VARS['PHP_SELF']) ? $HTTP_SERVER_VARS['PHP_SELF'] : $HTTP_SERVER_VARS['SCRIPT_NAME']);


// include the list of project filenames
require(DIR_WS_INCLUDES . 'filenames.php');

// include the list of project database tables
require(DIR_WS_INCLUDES . 'database_tables.php');

// Define how do we update currency exchange rates
// Possible values are 'oanda' 'xe' or ''
define('CURRENCY_SERVER_PRIMARY', 'oanda');
define('CURRENCY_SERVER_BACKUP', 'xe');

// include the database functions
require(DIR_WS_FUNCTIONS . 'database.php');

// make a connection to the database... now
tep_db_connect() or die('Unable to connect to database server!');
// set application wide parameters
$configuration_query = tep_db_query('select configuration_key as cfgKey, configuration_value as cfgValue from ' . TABLE_CONFIGURATION);
while ($configuration = tep_db_fetch_array($configuration_query)) {
	define($configuration['cfgKey'], $configuration['cfgValue']);
}


// define our general functions used application-wide
require(DIR_WS_FUNCTIONS . 'general.php');
require(DIR_WS_FUNCTIONS . 'html_output.php');


require('includes/functions/password_funcs.php');


require(DIR_WS_CLASSES . 'order.php');


// load the installed payment module
if (defined('MODULE_PAYMENT_INSTALLED') && tep_not_null(MODULE_PAYMENT_INSTALLED)) {
	$modules = explode(';', MODULE_PAYMENT_INSTALLED);

	$include_modules = array();

	if ( (tep_not_null($module)) && (in_array($module . '.' . substr($PHP_SELF, (strrpos($PHP_SELF, '.')+1)), $modules)) ) {
		$selected_module = $module;

		$include_modules[] = array('class' => $module, 'file' => $module . '.php');
	} else {
		reset($modules);
		while (list(, $value) = each($modules)) {
			$class = substr($value, 0, strrpos($value, '.'));
			$include_modules[] = array('class' => $class, 'file' => $value);
		}
	}
}


// search all languages for the payment method
$languages_query = tep_db_query("select directory from " . TABLE_LANGUAGES );
while ($languages = tep_db_fetch_array($languages_query)) {
	for ($i=0, $n=sizeof($include_modules); $i<$n; $i++) {
		$filename = "../" . DIR_WS_LANGUAGES . $languages[directory] . '/modules/payment/' . $include_modules[$i]['file'];	
		$paymentfile = fopen($filename,'r'); 

		while (!feof($paymentfile)){ 
			$zeile = fgets($paymentfile,1024);

			$pos1 = strpos($zeile, "('MODULE_PAYMENT_");
			$pos2 = strpos($zeile, "_TEXT_TITLE'");
			if ( ($pos1 > 0) && ($pos2 > 0)){
				$paymenttext = substr ( $zeile, $pos2 + 13 );
				$paymenttext = substr ( $paymenttext,strpos($paymenttext, "'")+1 );
				$paymenttext = substr ( $paymenttext, 0, strrpos($paymenttext, "'") );
				if ($paymenttext)
					$paymentsynonym[$paymenttext] = $include_modules[$i]['class'];
			}
		} 
		fclose($paymentfile); 
	}
}


// parse POST parameters
$getshipped = (isset($_REQUEST['getshipped']) ? $_REQUEST['getshipped'] : '');
$action = (isset($_REQUEST['action']) ? $_REQUEST['action'] : '');
$customer_notified = (isset($_REQUEST['customer_notified']) ? (int)$_REQUEST['customer_notified'] : 0); 
$comments = (isset($_REQUEST['comments']) ? $_REQUEST['comments'] : '');
$orderstosync = (isset($_REQUEST['setstate']) ? $_REQUEST['setstate'] : '{}');

$orderstosync = substr($orderstosync, 0, -1);
$orderstosync = substr($orderstosync, 1);
$orderstosync = explode(",", $orderstosync);

$username = tep_db_prepare_input($_REQUEST['username']);
$password = tep_db_prepare_input($_REQUEST['password']);

$check_query = tep_db_query("select id, user_name, user_password from " . TABLE_ADMINISTRATORS . " where user_name = '" . tep_db_input($username) . "'");


// generate header of OBDX response
echo ("<?xml version='1.0' encoding='UTF-8'?>\n");



// update the shop values
foreach ($orderstosync as $ordertosync) {

	list($orders_id_tosync, $orders_status_tosync) = explode("=", trim($ordertosync));

	if ($orders_status_tosync == 'pending')    $orders_status_tosync = 1;
	if ($orders_status_tosync == 'processing') $orders_status_tosync = 2;
	if ($orders_status_tosync == 'shipped')    $orders_status_tosync = 3;

	if (($orders_id_tosync > 0) && ($orders_status_tosync >= 1) && ($orders_status_tosync <= 3)){
		tep_db_query("update " . TABLE_ORDERS . " set orders_status = '".$orders_status_tosync. "' where orders_id = '" . (int)$orders_id_tosync . "'");
		tep_db_query("insert into " . TABLE_ORDERS_STATUS_HISTORY . " (orders_id, orders_status_id, date_added, customer_notified, comments) values ('" . (int)$orders_id_tosync . "', '" . $orders_status_tosync . "', now(), '" . $customer_notified . "', '" . $comments  . "')");
	}
}



// parse the GETSHIPPED parameter for the time interval
$getshipped = strtolower($getshipped);

if (preg_match('/\d+/', $getshipped, $matches)){
	$getshipped_number = $matches[0];
}

if (preg_match('/month|day|week|year|ever/', $getshipped, $matches)){
	$getshipped_datetype = $matches[0];
}

if (($getshipped_number > 0) && ($getshipped_datetype))
	$getshipped_condition = " or ( DATE_SUB(CURDATE(),INTERVAL ". $getshipped_number ." ". $getshipped_datetype." ) <= o.date_purchased) ";

if ($getshipped_datetype == 'ever')
	$getshipped_condition = " or TRUE";



if (tep_db_num_rows($check_query) == 1) {
	$check = tep_db_fetch_array($check_query);
	if (tep_validate_password($password, $check['user_password'])) {

		//password ok


		// generate OBDX response
		if ($action == 'getorders'){
			$check_orders_query = tep_db_query("select o.orders_id, o.orders_status, ot.text as order_total from " . TABLE_ORDERS . " o left join " . TABLE_ORDERS_TOTAL . " ot on (o.orders_id = ot.orders_id) where ot.class = 'ot_total' and (o.orders_status != '3' ". $getshipped_condition ."  ) ORDER BY o.orders_id DESC"); 

			echo ('<OPENTRANS xmlns="http://www.opentrans.org/XMLSchema/2.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opentrans.org/XMLSchema/2.1 opentrans_2_1.xsd" xmlns:bmecat="http://www.bmecat.org/bmecat/2005" xmlns:xmime="http://www.w3.org/2005/05/xmlmime">');
	
			while ($check_orders = tep_db_fetch_array($check_orders_query)) {

				$oID = $check_orders['orders_id'];
				$order = new order($oID);

				$payment_class = $paymentsynonym[ $order->info['payment_method'] ];
				$payment_text = $payment_class;

				if ($payment_class == 'cod') 					$payment_text = 'cod'; 	
				if ($payment_class == 'moneyorder') 			$payment_text = 'prepayment'; 	
				if ($payment_class == 'cc') 					$payment_text = 'creditcard'; 	
				if ($payment_class == 'authorizenet_cc_aim')	$payment_text = 'creditcard'; 	
				if ($payment_class == 'authorizenet_cc_sim') 	$payment_text = 'creditcard'; 	
				if ($payment_class == 'chronopay') 				$payment_text = 'chronopay.com'; 	
				if ($payment_class == 'ipayment_cc') 			$payment_text = 'ipayment.de'; 	
				if ($payment_class == 'nochex') 				$payment_text = 'nochex.com'; 	
				if ($payment_class == 'paypal_direct') 			$payment_text = 'paypal.com'; 	
				if ($payment_class == 'paypal_express') 		$payment_text = 'paypal.com'; 	
				if ($payment_class == 'paypal_standard') 		$payment_text = 'paypal.com'; 	
				if ($payment_class == 'paypal_uk_direct') 		$payment_text = 'paypal.com'; 	
				if ($payment_class == 'paypal_uk_express') 		$payment_text = 'paypal.com'; 	
				if ($payment_class == 'pm2checkout') 			$payment_text = '2checkout.com'; 	
				if ($payment_class == 'psigate') 				$payment_text = 'psigate.com'; 	
				if ($payment_class == 'secpay') 				$payment_text = 'secpay.com'; 	
				if ($payment_class == 'sofortueberweisung_direct') $payment_text = 'payment-networt.com'; 	
				if ($payment_class == 'worldpay_junior') 		$payment_text = 'bsworldpay.com'; 	

				$orders_history_query = tep_db_query("select orders_status_id, date_added, comments from " . TABLE_ORDERS_STATUS_HISTORY . " where orders_id = '" . tep_db_input($oID) . "' order by date_added");

echo ('<ORDER type="standard" version="2.1" xmlns="http://www.opentrans.org/XMLSchema/2.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opentrans.org/XMLSchema/2.1 opentrans_2_1.xsd" xmlns:bmecat="http://www.bmecat.org/bmecat/2005" xmlns:xmime="http://www.w3.org/2005/05/xmlmime">
	<ORDER_HEADER>');
				
				echo ("
				<ORDER_INFO>
			<ORDER_ID>$oID</ORDER_ID>");
				echo ("<ORDER_DATE>".str_replace(" ","T",$order->info['date_purchased'])."</ORDER_DATE><PARTIES>");


				if ($order->info['orders_status'] == 1) $order_status_text = "pending";
				if ($order->info['orders_status'] == 2) $order_status_text = "processing";
				if ($order->info['orders_status'] == 3) $order_status_text = "shipped";

//				echo ("order_status='". $order_status_text. "' ");
//				echo ("paymentmethod='". $payment_text ."' ");

				//echo ('    <currency_value>'.$order->info['currency_value'].'</currency_value>'."\n");
				//echo ('    <cc_type>'.$order->info['cc_type'].'</cc_type>'."\n");
				//echo ('    <cc_owner>'.$order->info['cc_owner'].'</cc_owner>'."\n");
				//echo ('    <cc_number>'.$order->info['cc_number'].'</cc_number>'."\n");
				//echo ('    <cc_expires>'.$order->info['cc_expires'].'</cc_expires>'."\n");
				//echo ('    <last_modified>'.$order->info['last_modified'].'</last_modified>'."\n");


				echo ("    				<PARTY>
					<bmecat:PARTY_ID type=\"supplier_specific\">980301</bmecat:PARTY_ID>
					<PARTY_ROLE>delivery</PARTY_ROLE>
					<ADDRESS>
											<CONTACT_DETAILS>
							<bmecat:CONTACT_ID>1</bmecat:CONTACT_ID>
							<bmecat:CONTACT_NAME>".utf8_encode($order->billing['name'])."</bmecat:CONTACT_NAME>");
							
				if (!empty($order->delivery['telephone'])) {
					echo ("			<bmecat:PHONE>".utf8_encode($order->delivery['telephone'])."</bmecat:PHONE>");
				}
							
				echo ("		</CONTACT_DETAILS>
						<bmecat:STREET>".utf8_encode($order->billing['street_address'])."</bmecat:STREET>
						<bmecat:ZIP>".utf8_encode($order->billing['postcode'])."</bmecat:ZIP>
						<bmecat:CITY>".utf8_encode($order->billing['city'])."</bmecat:CITY>
						<bmecat:STATE>Baden Württemberg</bmecat:STATE>
					</ADDRESS>
				</PARTY>
");
/*				echo ("name='' ");
				echo ("company='".utf8_encode($order->billing['company'])."' ");
				echo ("street='' ");
				echo ("zip='' ");
				echo ("city='' ");
				echo ("country='".utf8_encode($order->billing['country'])."' ");

				echo ("location='".utf8_encode($order->billing['postcode'])." ".utf8_encode($order->billing['city'])." ".utf8_encode($order->billing['country'])."' ");

				echo ("phone='".utf8_encode($order->billing['telephone'])."' ");
				echo ("email='".utf8_encode($order->billing['email_address'])."' ");

				//	      echo ("</contact>\n");
				//	      echo ("    <contact id='' type='delivery' "); 

				echo ("delivery_name='".utf8_encode($order->delivery['name'])."' ");
				echo ("delivery_company='".utf8_encode($order->delivery['company'])."' ");
				echo ("delivery_street='".utf8_encode($order->delivery['street_address'])."' ");
				echo ("delivery_zip='".utf8_encode($order->delivery['postcode'])."' ");
				echo ("delivery_city='".utf8_encode($order->delivery['city'])."' ");
				echo ("delivery_country='".utf8_encode($order->delivery['country'])."' ");
				echo ("delivery_phone='' ");
				echo ("delivery_email='".utf8_encode($order->delivery['email_address'])."'>");
				echo ("</contact>\n");

				//echo ('    <suburb>'.$order->customer['suburb'].'</suburb>'."\n");
				//echo ('    <state>'.$order->customer['state'].'</state>'."\n");
				//echo ('    <address_format_id>'.$order->customer['format_id'].'</address_format_id>'."\n");



				while ($orders_history = tep_db_fetch_array($orders_history_query)) {
					if (strlen(trim($orders_history['comments']))){
						echo ("    <comment date='" . $orders_history['date_added'] . "'>");
						echo ( utf8_encode(nl2br(tep_db_output($orders_history['comments']))));
						echo ("</comment>\n");
					}
				}*/

				echo("</PARTIES>
			<CUSTOMER_ORDER_REFERENCE>
			</CUSTOMER_ORDER_REFERENCE>
			<ORDER_PARTIES_REFERENCE>
			<bmecat:BUYER_IDREF>968314</bmecat:BUYER_IDREF>
			<bmecat:SUPPLIER_IDREF>980301</bmecat:SUPPLIER_IDREF>
			</ORDER_PARTIES_REFERENCE>
				
		</ORDER_INFO>

	</ORDER_HEADER>
	<ORDER_ITEM_LIST>
");

				foreach ($order->products as $product) {
					/*
	        		if ($product['attributes']){
	          			foreach ($product['attributes'] as $attribute) {
	            			echo ('        <attribute>'."\n");
	            			echo ('          <option>'.$product['attributes'][$attribute].'</option>'."\n");
	            			echo ('          <value>'.$product['attributes'][$attribute].'</value>'."\n");
	            			echo ('          <prefix>'.$product['attributes'][$attribute].'</prefix>'."\n");
	            			echo ('          <price>'.$product['attributes'][$attribute].'</price>'."\n");
	            			echo ('        </attribute>'."\n");
	          			}
	        		}
					*/
				
					$tax = 1+($product['tax']/100);
					/*
					echo ("    <item ");
					echo ("quantity='".$product['qty']."' ");
					echo ("currency='".$order->info['currency']."' ");
					echo ("name='".utf8_encode($product['model'])."' ");
					echo ("description='".utf8_encode($product['name']));
					echo ("total='".number_format( $product['qty'] * $product['final_price'], 2) ."' ");
					echo ("totalgross='".number_format( $product['qty'] * $product['final_price'] * $tax, 2)."' ");
					echo ("vatfactor='". number_format($tax,2) . "' ");
					*/

				echo ("    		<ORDER_ITEM>
			<LINE_ITEM_ID>1</LINE_ITEM_ID>
			<PRODUCT_ID>
				<bmecat:SUPPLIER_PID>NB-A4</bmecat:SUPPLIER_PID>
				<bmecat:DESCRIPTION_SHORT>{$product['model']}</bmecat:DESCRIPTION_SHORT>
				<bmecat:DESCRIPTION_LONG>{$product['name']}</bmecat:DESCRIPTION_LONG>
			</PRODUCT_ID>


			<QUANTITY>{$product['qty']}</QUANTITY>
			<bmecat:ORDER_UNIT>C62</bmecat:ORDER_UNIT>
			<PRODUCT_PRICE_FIX>
				<bmecat:PRICE_AMOUNT>{$product['final_price']}</bmecat:PRICE_AMOUNT>

			</PRODUCT_PRICE_FIX>
			<PRICE_LINE_AMOUNT>".number_format( $shipping_value * $tax, 2)."</PRICE_LINE_AMOUNT>

		</ORDER_ITEM>
 ");
				}

				$totals_query = tep_db_query("select title, text, class from " . TABLE_ORDERS_TOTAL . " where orders_id = '" . (int)$oID . "' order by sort_order");
				while ($totals = tep_db_fetch_array($totals_query)) {
					$totals_a[] = array('title' => $totals['title'],
							'text' => $totals['text'],
							'class' => $totals['class']);

				}

				for ($i = 0, $n = sizeof($totals_a); $i < $n; $i++) {
					if ($totals_a[$i]['class'] == 'ot_shipping'){
						$shipping_title = $totals_a[$i]['title'];
						$shipping_text = $totals_a[$i]['text'];
					}
				}

				// delete last character, if it is a ":"
				if (substr($shipping_title, -1, 1) == ':')
					$shipping_title = substr($shipping_title, 0, -1);
				;

				if (preg_match("/[0-9]\.[0-9]+/",$shipping_text,$matches))
					$shipping_value = $matches[0];

 /*
				echo ("currency='".$order->info['currency']."' ");
				echo ("name='shipping' ");
				echo ("description='".utf8_encode($shipping_title)."' ");
				// no tax for shipping
				// $tax = 1+($product['tax']/100);
				$tax = 1.00;
				echo ("total='" .number_format( $shipping_value, 2)."' ");
				echo ("totalgross='".number_format( $shipping_value * $tax, 2)."' ");
				echo ("vatfactor='". number_format($tax,2) . "' ");
				echo (">");



				echo ("  </transaction>\n");*/
			echo (" 	</ORDER_ITEM_LIST>
	<ORDER_SUMMARY>
		<TOTAL_ITEM_NUM>1</TOTAL_ITEM_NUM>
		<TOTAL_AMOUNT>".utf8_encode(strip_tags(str_replace("$","",str_replace(",","",$check_orders['order_total']))))."</TOTAL_AMOUNT>

	</ORDER_SUMMARY>
</ORDER>\n");
			}
		echo ('</OPENTRANS>');

		}	
		else {
			echo (" <error>no valid action set</error>\n");
		}

	}
	else{
		echo (" <error>invalid username or password</error>\n");
	}    
}
else
	echo (" <error>enter unsername and password</error>\n");


?>