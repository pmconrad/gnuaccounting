<?php


$client = new SoapClient('http://127.0.0.1/magento/api/?wsdl');

//username must be API user and password must be API key
if ((!empty($_REQUEST["username"])) && (!empty($_REQUEST["password"]))) {
    if ($_REQUEST['action'] == "getorders") {
        $session = $client->login($_REQUEST["username"], $_REQUEST["password"]);
        $orders = $client->call($session, 'sales_order.list', array());

        echo ('<OPENTRANS xmlns="http://www.opentrans.org/XMLSchema/2.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opentrans.org/XMLSchema/2.1 opentrans_2_1.xsd" xmlns:bmecat="http://www.bmecat.org/bmecat/2005" xmlns:xmime="http://www.w3.org/2005/05/xmlmime">');


        foreach ($orders as $order) {
            $orderDetails = $client->call($session, 'sales_order.info', $order["increment_id"]);
            echo ('<ORDER type="standard" version="2.1" xmlns="http://www.opentrans.org/XMLSchema/2.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opentrans.org/XMLSchema/2.1 opentrans_2_1.xsd" xmlns:bmecat="http://www.bmecat.org/bmecat/2005" xmlns:xmime="http://www.w3.org/2005/05/xmlmime">
	<ORDER_HEADER>');
            echo ("
				<ORDER_INFO>
			<ORDER_ID>{$order["increment_id"]}</ORDER_ID>");
            echo ("<ORDER_DATE>" . str_replace(" ", "T", $order['created_at']) . "</ORDER_DATE><PARTIES>");
            echo ("    				<PARTY>
					<bmecat:PARTY_ID type=\"supplier_specific\">980301</bmecat:PARTY_ID>
					<PARTY_ROLE>delivery</PARTY_ROLE>
					<ADDRESS>
											<CONTACT_DETAILS>
							<bmecat:CONTACT_ID>1</bmecat:CONTACT_ID>
							<bmecat:CONTACT_NAME> " . utf8_encode($orderDetails['shipping_address']["firstname"]) . " " . utf8_encode($orderDetails['shipping_address']["lastname"]) . "</bmecat:CONTACT_NAME>");

            if (!empty($orderDetails['shipping_address']["telephone"])) {
                echo ("			<bmecat:PHONE>" . utf8_encode($orderDetails['shipping_address']["telephone"]) . "</bmecat:PHONE>");
            }

            echo ("		</CONTACT_DETAILS>
						<bmecat:STREET>" . utf8_encode($orderDetails['shipping_address']["street"]) . "</bmecat:STREET>
						<bmecat:ZIP>" . utf8_encode($orderDetails['shipping_address']["postcode"]) . "</bmecat:ZIP>
						<bmecat:CITY>" . utf8_encode($orderDetails['shipping_address']["city"]) . "</bmecat:CITY>
						<bmecat:STATE>" . utf8_encode($orderDetails['shipping_address']["region"]) . "</bmecat:STATE>
					</ADDRESS>
				</PARTY>
");
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

            foreach ($orderDetails['items'] as $item) {
                echo ("    		<ORDER_ITEM>
			<LINE_ITEM_ID>{$item['item_id']}</LINE_ITEM_ID>
			<PRODUCT_ID>
				<bmecat:SUPPLIER_PID>NB-A4</bmecat:SUPPLIER_PID>
				<bmecat:DESCRIPTION_SHORT>{$item['name']}</bmecat:DESCRIPTION_SHORT>
				<bmecat:DESCRIPTION_LONG>{$item['description']}</bmecat:DESCRIPTION_LONG>
			</PRODUCT_ID>


			<QUANTITY>{$item['qty_ordered']}</QUANTITY>
			<bmecat:ORDER_UNIT>C62</bmecat:ORDER_UNIT>
			<PRODUCT_PRICE_FIX>
				<bmecat:PRICE_AMOUNT>{$item["price"]}</bmecat:PRICE_AMOUNT>

			</PRODUCT_PRICE_FIX>
			<PRICE_LINE_AMOUNT>" . number_format($item["row_total_incl_tax"], 2) . "</PRICE_LINE_AMOUNT>

		</ORDER_ITEM>
 ");
            }
            echo (" 	</ORDER_ITEM_LIST>
	<ORDER_SUMMARY>
		<TOTAL_ITEM_NUM>1</TOTAL_ITEM_NUM>
		<TOTAL_AMOUNT>" . utf8_encode(strip_tags(str_replace(",", "", $order['base_grand_total']))) . "</TOTAL_AMOUNT>

	</ORDER_SUMMARY>
</ORDER>\n");


            echo ('</OPENTRANS>');
        }

    } else {
        echo (" <error>no valid action set</error>\n");

    }
} else {
    echo (" <error>invalid username or password</error>\n");

}

$client->endSession($session);
?>