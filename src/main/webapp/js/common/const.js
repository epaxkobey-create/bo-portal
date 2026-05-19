if (typeof (CurrencyType) == 'undefined') {
    CurrencyType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CurrencyType.USD = {
        "value": 15,
        "currencyName": "USD",
        "symbol": "$"
    };
    CurrencyType.CAD = {
        "value": 25,
        "currencyName": "CAD",
        "symbol": "$"
    };
    CurrencyType.EUR = {
        "value": 26,
        "currencyName": "EUR",
        "symbol": "€"
    };

    (function () {
        for (atr in CurrencyType) {
            var obj = CurrencyType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.displayName = 'form.text.currencyType.name.' + atr;
            obj.unique = getValue;
        }
    })();

    CurrencyType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CurrencyType.getByName = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]['name'].toLowerCase() == value.toLowerCase()) {
                return objects[i];
            }
        }
        return null;
    }

    CurrencyType.getAllCurrencyTypes = function () {
        return objects;
    };
})();

/*
 */
if (typeof (CallingCodeType) == 'undefined') {
    CallingCodeType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CallingCodeType.Afghanistan = {
        'value': 93,
    };
    CallingCodeType.Malaysia = {
        'value': 60,
    };
    CallingCodeType.Maldives = {
        'value': 960,
    };
    CallingCodeType.Bangladesh = {
        'value': 880,
    };
    CallingCodeType.Philippines = {
        'value': 63,
    };
    CallingCodeType.Bhutan = {
        'value': 975,
    };
    CallingCodeType.Nepal = {
        'value': 977,
    };
    CallingCodeType.Brunei = {
        'value': 673,
    };
    CallingCodeType.Myanmar = {
        'value': 95,
    };
    CallingCodeType.Pakistan = {
        'value': 92,
    };
    CallingCodeType.Mexico = {
        'value': 52,
    };
    CallingCodeType.US = {
        'value': 1,
    };
    CallingCodeType.Cambodia = {
        'value': 855,
    };
    CallingCodeType.China = {
        'value': 86,
    };
    CallingCodeType.Singapore = {
        'value': 65,
    };
    CallingCodeType.SouthKorea = {
        'value': 82,
    };
    CallingCodeType.India = {
        'value': 91,
    };
    CallingCodeType.Indonesia = {
        'value': 62,
    };
    CallingCodeType.SriLanka = {
        'value': 94,
    };
    CallingCodeType.Thailand = {
        'value': 66,
    };
    CallingCodeType.Vietnam = {
        'value': 84,
    };
    CallingCodeType.Laos = {
        'value': 856,
    };
    CallingCodeType.Brazil = {
        'value': 55,
    };
    CallingCodeType.Nigeria = {
        'value': 234,
    };
    CallingCodeType.ZA = {
        'value': 27,
    };
    CallingCodeType.GH = {
        'value': 233,
    };
    CallingCodeType.Australia = {
        'value': 61,
    };
    CallingCodeType.NewZealand = {
        'value': 64,
    };
    CallingCodeType.Iran = {
        'value': 98,
    };
    CallingCodeType.Canada = {
        'value': 1,
    };
    CallingCodeType.HongKong = {
        'value': 852,
    };


    (function () {
        for (var atr in CallingCodeType) {
            var obj = CallingCodeType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
        }
    })();

    CallingCodeType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]['value'] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CallingCodeType.getByName = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]['name'].toLowerCase() == value.toLowerCase()) {
                return objects[i];
            }
        }
        return null;
    };

    CallingCodeType.getByCurrency = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]['currency'].value == value) {
                return objects[i];
            }
        }
        return null;
    };

})();

/*
 */
if (typeof (CountryType) == 'undefined') {
    CountryType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CountryType.US = {
        'value': 13,
        'currency': CurrencyType.USD,
        'callingCode': 855
    };
    CountryType.CA = {
        'value': 27,
        'currency': CurrencyType.CAD,
        'callingCode': 1
    };

    (function () {
        for (atr in CountryType) {
            var obj = CountryType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
        }
    })();

    CountryType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]['value'] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CountryType.getByName = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]['name'].toLowerCase() == value.toLowerCase()) {
                return objects[i];
            }
        }
        return null;
    };

    CountryType.getByCurrency = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]['currency'].value == value) {
                return objects[i];
            }
        }
        return null;
    };

    CountryType.values = function () {
        return objects;
    };

})();

/*
 */
if (typeof (ManagerStatusType) == 'undefined') {
    ManagerStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ManagerStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        }
    };
    ManagerStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in ManagerStatusType) {
            var obj = ManagerStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ManagerStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ManagerStatusType.values = function () {
        return objects;
    };
})();

if (typeof (GameType) == 'undefined') {
    GameType = {};
}

(function() {
    var getValue = function() {
        return this.value;
    };
    var objects = [];
    var uniques = [];

    GameType.SLOT = {
        "value": 1,
        "getShortName": function() {
            return "Slot";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.slot";
        },
        "isCreateCategory": true,
    };
    GameType.CASINO = {
        "value": 2,
        "getShortName": function() {
            return "Casino";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.casino";
        },
        "isCreateCategory": true,
    };
    GameType.Sport = {
        "value": 4,
        "getShortName": function() {
            return "Sport";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.sport";
        },
        "isCreateCategory": false,
    };
    GameType.FH = {
        "value": 8,
        "getShortName": function() {
            return "Fish";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.fish";
        },
        "isCreateCategory": true,
    };
    GameType.CARD = {
        "value": 16,
        "getShortName": function() {
            return "Card";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.card";
        },
        "isCreateCategory": false,
    };
    GameType.ESport = {
        "value": 32,
        "getShortName": function() {
            return "ESport";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.esport";
        },
        "isCreateCategory": false,
    };
    GameType.LOTTERY = {
        "value": 64,
        "getShortName": function() {
            return "Lottery";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.lottery";
        },
        "isCreateCategory": true,
    };
    GameType.P2P = {
        "value": 128,
        "getShortName": function() {
            return "P2P";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.p2p";
        },
        "isCreateCategory": false,
    };
    GameType.TABLE = {
        "value": 256,
        "getShortName": function() {
            return "Table";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.table";
        },
        "isCreateCategory": true,
    };
    GameType.OTHERS = {
        "value": 512,
        "getShortName": function() {
            return "Others";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.others";
        },
        "isCreateCategory": false,
    };
    GameType.ARCADE = {
        "value": 1024,
        "getShortName": function() {
            return "Arcade";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.arcade";
        },
        "isCreateCategory": true,
    };
    GameType.COCK_FIGHTING = {
        "value": 2048,
        "getShortName": function() {
            return "CockFighting";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.cockfighting";
        },
        "isCreateCategory": true,
    };
    GameType.RAIN = {
        "value": 4096,
        "getShortName": function() {
            return "Rain";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.rain";
        },
        "isCreateCategory": true,
    };
    GameType.CRASH = {
        "value": 8192,
        "getShortName": function() {
            return "Crash";
        },
        "getFullName": function() {
            return "fe.text.fe.game_type.crash";
        },
        "isCreateCategory": true,
    };

    (function() {
        for (atr in GameType) {
            var obj = GameType[atr];
            if (obj === GameType.OTHERS) {
                continue;
            }
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;

            uniques.push(obj.value);
        }
        //others
        var objOthers = GameType.OTHERS;
        objects[objects.length] = objOthers;
        objOthers.name = 'OTHERS';
        objOthers.unique = GameType.OTHERS.value;

        uniques.push(objOthers.value);

    })();

    GameType.getInstanceOf = function(value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    GameType.values = function() {
        return objects;
    };

    GameType.uniques = function() {
        return uniques;
    };

})();

if (typeof (CompanyBankBankStatusType) == 'undefined') {
    CompanyBankBankStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CompanyBankBankStatusType.INACTIVE = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    CompanyBankBankStatusType.CUSTOMIZE = {
        "value": 0,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Customize";
        }
    };
    CompanyBankBankStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in CompanyBankBankStatusType) {
            var obj = CompanyBankBankStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CompanyBankBankStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CompanyBankBankStatusType.values = function () {
        return objects;
    };
})();

if (typeof (CompanyBankPurposeType) == 'undefined') {
    CompanyBankPurposeType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CompanyBankPurposeType.INFLOW = {
        "value": 1,
        "getName": function () {
            return "Inflow";
        }
    };
    CompanyBankPurposeType.OUTFLOW = {
        "value": 2,
        "getName": function () {
            return "Outflow";
        }
    };


    (function () {
        for (atr in CompanyBankPurposeType) {
            var obj = CompanyBankPurposeType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CompanyBankPurposeType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CompanyBankPurposeType.values = function () {
        return objects;
    };

    CompanyBankPurposeType.isInflow = function (purpose) {
        return (purpose & CompanyBankPurposeType.INFLOW.value) == CompanyBankPurposeType.INFLOW.value;
    };

    CompanyBankPurposeType.isOutflow = function (purpose) {
        return (purpose & CompanyBankPurposeType.OUTFLOW.value) == CompanyBankPurposeType.OUTFLOW.value;
    };

})();

if (typeof (AffiliateStatusType) == 'undefined') {
    AffiliateStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AffiliateStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    AffiliateStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in AffiliateStatusType) {
            var obj = AffiliateStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AffiliateStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AffiliateStatusType.values = function () {
        return objects;
    };
})();

if (typeof (AffiliateDomainStatusType) == 'undefined') {
    AffiliateDomainStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AffiliateDomainStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    AffiliateDomainStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in AffiliateDomainStatusType) {
            var obj = AffiliateDomainStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AffiliateDomainStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AffiliateDomainStatusType.values = function () {
        return objects;
    };
})();

if (typeof (AffiliateCategoryType) == 'undefined') {
    AffiliateCategoryType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AffiliateCategoryType.LANDING_PAGE = {
        "value": 0,
        "getName": function () {
            return "Landing page";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.landingPage";
        }
    };
    AffiliateCategoryType.MAIN_PAGE = {
        "value": 1,
        "getName": function () {
            return "Main page";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.mainPage";
        }
    };
    AffiliateCategoryType.EMAIL = {
        "value": 2,
        "getName": function () {
            return "Email";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.email";
        }
    };
    AffiliateCategoryType.SMS = {
        "value": 3,
        "getName": function () {
            return "SMS";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.sms";
        }
    };
    AffiliateCategoryType.QQ = {
        "value": 4,
        "getName": function () {
            return "QQ";
        },
        "getDisplayName": function () {
            return "QQ";
        }
    };
    AffiliateCategoryType.FORUM = {
        "value": 5,
        "getName": function () {
            return "Forum";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.forum";
        }
    };
    AffiliateCategoryType.SALES = {
        "value": 6,
        "getName": function () {
            return "Sales";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.sales";
        }
    };
    AffiliateCategoryType.WECHAT = {
        "value": 7,
        "getName": function () {
            return "WeChat";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.wechat";
        }
    };
    AffiliateCategoryType.OTHER = {
        "value": 99,
        "getName": function () {
            return "Other";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.categoryType.other";
        }
    };


    (function () {
        for (atr in AffiliateCategoryType) {
            var obj = AffiliateCategoryType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AffiliateCategoryType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AffiliateCategoryType.values = function () {
        return objects;
    };
})();

if (typeof (MoneyTransactionStatusType) == 'undefined') {
    MoneyTransactionStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    MoneyTransactionStatusType.NEW = {
        "value": 0,
        "getClassName": function () {
            return "label-primary";
        },
        "getName": function () {
            return "Processing";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.NEW";
        },
        "getTxnClassName": function () {
            return "state-pending";
        },
    };
    MoneyTransactionStatusType.PENDING_APPROVAL = {
        "value": 1,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Verified";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.PENDING_APPROVAL";
        },
        "getTxnClassName": function () {
            return "state-pending";
        },
    };
    MoneyTransactionStatusType.REJECTED = {
        "value": -1,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Fail";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.REJECTED";
        },
        "getTxnClassName": function () {
            return "state-rejected";
        },
    };
    MoneyTransactionStatusType.CONFIRMED = {
        "value": 2,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Success";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.CONFIRMED";
        },
        "getTxnClassName": function () {
            return "state-approved";
        },
    };
    MoneyTransactionStatusType.CLOSE = {
        "value": -2,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Disapproved";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.CLOSE";
        },
        "getTxnClassName": function () {
            return "state-rejected";
        },
    };
    MoneyTransactionStatusType.ON_HOLD = {
        "value": 3,
        "getClassName": function () {
            return "label-hold";
        },
        "getName": function () {
            return "On Hold";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.ON_HOLD";
        },
        "getTxnClassName": function () {
            return "state-pending";
        },
    };
    MoneyTransactionStatusType.PROCESSING = {
        "value": 4,
        "getClassName": function () {
            return "label-processing";
        },
        "getName": function () {
            return "Processing";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.PROCESSING";
        },
        "getTxnClassName": function () {
            return "state-pending";
        },
    };
    MoneyTransactionStatusType.REVERTED = {
        "value": -3,
        "getClassName": function () {
            return "label-revert";
        },
        "getName": function () {
            return "Reverted";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.REVERTED";
        },
        "getTxnClassName": function () {
            return "state-reverted";
        },
    };
    MoneyTransactionStatusType.AWAITED = {
        "value": 5,
        "getClassName": function () {
            return "label-await";
        },
        "getName": function () {
            return "Awaited";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.AWAITED";
        },
        "getTxnClassName": function () {
            return "state-pending";
        },
    };
    MoneyTransactionStatusType.PGPROCESSING = {
        "value": 6,
        "getClassName": function () {
            return "label-processing";
        },
        "getName": function () {
            return "Processing";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.PROCESSING";
        },
        "getTxnClassName": function () {
            return "state-pending";
        },
    };


    (function () {
        for (atr in MoneyTransactionStatusType) {
            var obj = MoneyTransactionStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    MoneyTransactionStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MoneyTransactionStatusType.values = function () {
        return objects;
    };
})();

if (typeof (SyncTaskStatusType) == 'undefined') {
    SyncTaskStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    SyncTaskStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    SyncTaskStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };

    (function () {
        for (atr in SyncTaskStatusType) {
            var obj = SyncTaskStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    SyncTaskStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    SyncTaskStatusType.values = function () {
        return objects;
    };
})();

if (typeof (GameStatusType) == 'undefined') {
    GameStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    GameStatusType.AUTO_CREATED = {
        "value": -2,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Auto Created";
        }
    };
    GameStatusType.INVISIBLE = {
        "value": -1,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Invisible";
        }
    };
    GameStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    GameStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in GameStatusType) {
            var obj = GameStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    GameStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    GameStatusType.values = function () {
        return objects;
    };
})();

if (typeof (WebsiteStatusType) == 'undefined') {
    WebsiteStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    WebsiteStatusType.html = function (status) {
        var instance = WebsiteStatusType.getInstanceOf(status);
        return '<span class="label ' + instance.getClassName() + '">' + instance.getName() + '</span>';
    };

    WebsiteStatusType.INACTIVE = {
        "value": -1,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    WebsiteStatusType.MAINTENANCE = {
        "value": 0,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Maintenance";
        }
    };
    WebsiteStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in WebsiteStatusType) {
            var obj = WebsiteStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WebsiteStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    WebsiteStatusType.values = function () {
        return objects;
    };
})();

if (typeof (RankingType) == 'undefined') {
    RankingType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    RankingType.MANUAL = {
        "value": 0,
        "getName": function () {
            return "Manual";
        }
    };
    RankingType.AUTO = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Auto";
        }
    };


    (function () {
        for (atr in RankingType) {
            var obj = RankingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    RankingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    RankingType.values = function () {
        return objects;
    };
})();

if (typeof (ProviderStatusType) == 'undefined') {
    ProviderStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ProviderStatusType.html = function (status) {
        var instance = ProviderStatusType.getInstanceOf(status);
        return '<span class="label ' + instance.getClassName() + '">' + instance.getName() + '</span>';
    };

    ProviderStatusType.INACTIVE = {
        "value": -1,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        }
    };
    ProviderStatusType.MAINTENANCE = {
        "value": 0,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Maintenance";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.maintenance";
        }
    };
    ProviderStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in ProviderStatusType) {
            var obj = ProviderStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ProviderStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ProviderStatusType.values = function () {
        return objects;
    };
})();

if (typeof (VendorStatusType) == 'undefined') {
    VendorStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    VendorStatusType.html = function (status) {
        var instance = VendorStatusType.getInstanceOf(status);
        return '<span class="label ' + instance.getClassName() + '">' + instance.getName() + '</span>';
    };

    VendorStatusType.INVISIBLE = {
        "value": -1,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Invisible";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.invisible";
        }
    };
    VendorStatusType.MAINTENANCE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Maintenance";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.maintenance";
        }
    };
    VendorStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in VendorStatusType) {
            var obj = VendorStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    VendorStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    VendorStatusType.values = function () {
        return objects;
    };
})();


if (typeof (BonusTemplateStatusType) == 'undefined') {
    BonusTemplateStatusType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    BonusTemplateStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        }
    };
    BonusTemplateStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in BonusTemplateStatusType) {
            var obj = BonusTemplateStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BonusTemplateStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BonusTemplateStatusType.values = function () {
        return objects;
    };
})();

if (typeof (CommissionStructureStatusType) == 'undefined') {
    CommissionStructureStatusType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CommissionStructureStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        }
    };
    CommissionStructureStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in CommissionStructureStatusType) {
            var obj = CommissionStructureStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CommissionStructureStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CommissionStructureStatusType.values = function () {
        return objects;
    };
})();

if (typeof (MessageType) == 'undefined') {
    MessageType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    MessageType.TOP_BANNER = {
        "value": 0,
        "getName": function () {
            return "Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.TOPBANNER";
        },
        "imageFieldName": {
            sliderName: 'announce',
            h5SliderName: 'h5Announce'
        }
    };
    MessageType.RUNNING = {
        "value": 1,
        "getName": function () {
            return "Running";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.RUNNING";
        },
        "imageFieldName": {}
    };
    MessageType.MEMBER = {
        "value": 2,
        "getName": function () {
            return "Member";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.MEMBER";
        },
        "imageFieldName": {}
    };
    MessageType.POP = {
        "value": 3,
        "getName": function () {
            return "Pop up";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.POPUP";
        },
        "imageFieldName": {
            bannerName: 'banner',
            h5SliderName: 'h5Banner'
        }
    };
    MessageType.SYSTEM = {
        "value": 4,
        "getName": function () {
            return "System";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SYSTEM";
        },
        "imageFieldName": {}
    };
    MessageType.PROMOTION = {
        "value": 5,
        "getName": function () {
            return "Promotion";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.PROMOTION";
        },
        "imageFieldName": {
            sliderName: 'icon',
            bannerName: 'banner',
            h5SliderName: 'h5Icon'
        }
    };
    MessageType.SLOT_SLIDER = {
        "value": 6,
        "getName": function () {
            return "Slot Slider";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SLOTSLIDER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'slider'
        }
    };
    MessageType.SLOT_TOP_BANNER = {
        "value": 7,
        "getName": function () {
            return "Slot Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SLOTTOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'slotSlider',
            h5SliderName: 'slotH5Slider'
        }
    };
    MessageType.EMAIL = {
        "value": 8,
        "getName": function () {
            return "Email";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.EMAIL";
        },
        "imageFieldName": {
            sliderName: 'announce'
        }
    };
    MessageType.SMS = {
        "value": 9,
        "getName": function () {
            return "SMS";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SMS";
        },
        "imageFieldName": {}
    };
    MessageType.FEATURE_GAME = {
        "value": 10,
        "getName": function () {
            return "Feature Game";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.games.featuredGame";
        },
        "imageFieldName": {
            sliderName: 'icon',
            h5SliderName: 'h5Icon'
        }
    };
    MessageType.RANK_WINNER = {
        "value": 11,
        "getName": function () {
            return "Rank Winner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.RANK_WINNER";
        },
        "imageFieldName": {}
    };
    MessageType.SPORT_SLIDER = {
        "value": 12,
        "getName": function () {
            return "Sport Slider";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SPORTSLIDER";
        },
        "imageFieldName": {
            sliderName: 'sportSlider',
        }
    };
    MessageType.CASINO_TOP_BANNER = {
        "value": 13,
        "getName": function () {
            return "Casino Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.CASINOTOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'announce'
        }
    };
    MessageType.PROMOTION_TOP_BANNER = {
        "value": 14,
        "getName": function () {
            return "Promotion Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.PROMOTIONTOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'announce'
        }
    };
    MessageType.TABLE_TOP_BANNER = {
        "value": 15,
        "getName": function () {
            return "Table Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.TABLETOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'tableSlider',
            h5SliderName: 'tableH5Slider'
        }
    };
    MessageType.REGISTER_SLIDER = {
        "value": 16,
        "getName": function () {
            return "Register Slider";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.REGISTERSLIDER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'registerSlider',
            h5SliderName: 'registerH5Slider'
        }
    };
    MessageType.REGISTER_SUCCESS_SLIDER = {
        "value": 17,
        "getName": function () {
            return "Register Success Slider";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.REGISTERSUCCESSSLIDER";
        },
        "imageFieldName": {
            sliderName: 'registerSuccessSlider',
        }
    };
    MessageType.SOCIAL_MEDIA = {
        "value": 18,
        "getName": function () {
            return "Social Media";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SOCIALMEDIA";
        },
        "imageFieldName": {
            fieldName: 'socialMedia',
        }
    };
//	MessageType.PAYMENT_PARTNERS = {
//		"value" : 19,
//		"getName" : function() { return "Payment Partners"; },
//		"getDisplayName" : function() { return "form.text.backOffice.messageType.PAYMENTPARTNERS"; },
//		"imageFieldName" : {
//			fieldName : 'paymentPartners',
//		}
//	};
    MessageType.RANK_BONUS = {
        "value": 20,
        "getName": function () {
            return "Rank Bonus";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.RANK_BONUS";
        },
        "imageFieldName": {}
    };
    MessageType.RANK_PAYMENT = {
        "value": 21,
        "getName": function () {
            return "Rank Payment";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.RANK_PAYMENT";
        },
        "imageFieldName": {}
    };
    MessageType.REFERRAL_TEAMS_AND_CONDITIONS = {
        "value": 22,
        "getName": function () {
            return "Terms & Conditions";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.REFERRAL_TEAMS_AND_CONDITIONS";
        },
        "imageFieldName": {}
    };
    MessageType.SPORT_TOP_BANNER = {
        "value": 23,
        "getName": function () {
            return "Sport Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SPORTTOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'sportTBSlider',
            h5SliderName: 'sportH5Slider'
        }
    };
    MessageType.FISH_TOP_BANNER = {
        "value": 24,
        "getName": function () {
            return "Fish Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.FISHTOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'fishSlider',
            h5SliderName: 'fishH5Slider'
        }
    };
    MessageType.ARCADE_TOP_BANNER = {
        "value": 25,
        "getName": function () {
            return "Arcade Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.ARCADETOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'arcadeSlider',
            h5SliderName: 'arcadeH5Slider'
        }
    };
    MessageType.FOOTER_FLOAT_BANNER = {
        "value": 26,
        "getName": function () {
            return "Footer Float Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.FOOTERFLOATBANNER";
        },
        "imageFieldName": {
            bannerName: 'footerFloatSubBanner',
            sliderName: 'footerFloatBanner',
            h5SliderName: 'footerH5FloatBanner'
        }
    };
    MessageType.LOTTERY_TOP_BANNER = {
        "value": 27,
        "getName": function () {
            return "Lottery Top Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.LOTTERYTOPBANNER";
        },
        "imageFieldName": {
            bannerName: 'banner',
            sliderName: 'lotterySlider',
            h5SliderName: 'lotteryH5Slider'
        }
    };
    MessageType.SIDE_MENU_BANNER = {
        "value": 28,
        "getName": function () {
            return "Side Menu Banner";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.SIDEMENUBANNER";
        },
        "imageFieldName": {
            sliderName: 'announce',
            h5SliderName: 'h5Announce'
        }
    };
    MessageType.REFERRAL_RANKING_LIST = {
        "value": 29,
        "getName": function () {
            return "Referral Ranking List";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.messageType.REFERRALRANKINGLIST";
        },
        "imageFieldName": {}
    };

    (function () {
        for (atr in MessageType) {
            var obj = MessageType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    MessageType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MessageType.values = function () {
        return objects;
    };
})();

if (typeof (MessageContentType) == 'undefined') {
    MessageContentType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    MessageContentType.TEXT = {
        "value": 0,
        "getName": function () {
            return "Text";
        }
    };
    MessageContentType.LINK = {
        "value": 1,
        "getName": function () {
            return "Link";
        }
    };
//	MessageContentType.IMAGE = {
//		"value" : 2,
//		"getName" : function() { return "Image"; }
//	};


    (function () {
        for (atr in MessageContentType) {
            var obj = MessageContentType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    MessageContentType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MessageContentType.values = function () {
        return objects;
    };
})();

if (typeof (MessageRoleType) == 'undefined') {
    MessageRoleType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    MessageRoleType.GUEST = {
        "value": 1,
        "getName": function () {
            return "Guest";
        }
    };

    MessageRoleType.PLAYER = {
        "value": 2,
        "getName": function () {
            return "Player";
        }
    };

    (function () {
        for (atr in MessageRoleType) {
            var obj = MessageRoleType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    MessageRoleType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MessageRoleType.values = function () {
        return objects;
    };
})();


if (typeof (MessageStatusType) == 'undefined') {
    MessageStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    MessageStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        },
    };
    MessageStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        },
    };


    (function () {
        for (atr in MessageStatusType) {
            var obj = MessageStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    MessageStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MessageStatusType.values = function () {
        return objects;
    };
})();

if (typeof (CustomerServiceStatusType) == 'undefined') {
    CustomerServiceStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CustomerServiceStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        }
    };
    CustomerServiceStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in CustomerServiceStatusType) {
            var obj = CustomerServiceStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CustomerServiceStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CustomerServiceStatusType.values = function () {
        return objects;
    };
})();

if (typeof (SendBoxType) == 'undefined') {
    SendBoxType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    SendBoxType.NO = {
        "value": 0,
        "getName": function () {
            return "No";
        }
    };
    SendBoxType.YES = {
        "value": 1,
        "getName": function () {
            return "Yes";
        }
    };


    (function () {
        for (atr in SendBoxType) {
            var obj = SendBoxType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    SendBoxType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    SendBoxType.values = function () {
        return objects;
    };
})();

if (typeof (ManagerRoleStatusType) == 'undefined') {
    ManagerRoleStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ManagerRoleStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        }
    };
    ManagerRoleStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        }
    };


    (function () {
        for (atr in ManagerRoleStatusType) {
            var obj = ManagerRoleStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ManagerRoleStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ManagerRoleStatusType.values = function () {
        return objects;
    };
})();

if (typeof (BonusAwardingType) == 'undefined') {
    BonusAwardingType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    BonusAwardingType.MANUAL = {
        "value": 0,
        "getName": function () {
            return "Manual";
        }
    };
    BonusAwardingType.DEPOSIT = {
        "value": 1,
        "getName": function () {
            return "Deposit";
        }
    };
    BonusAwardingType.TURNOVER = {
        "value": 2,
        "getName": function () {
            return "Turnover";
        }
    };
    BonusAwardingType.LOSS = {
        "value": 3,
        "getName": function () {
            return "Loss";
        }
    };
    BonusAwardingType.SPECIAL = {
        "value": 4,
        "getName": function () {
            return "Special";
        }
    };


    (function () {
        for (atr in BonusAwardingType) {
            var obj = BonusAwardingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BonusAwardingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BonusAwardingType.values = function () {
        return objects;
    };
})();

if (typeof (AwardingMethodType) == 'undefined') {
    AwardingMethodType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AwardingMethodType.MANUAL = {
        "value": 0,
        "getName": function () {
            return "Manual";
        },
        "getDisplay": function () {
            return "form.text.backOffice.awardingMethodType.MANUAL";
        }
    };
    AwardingMethodType.AUTOMATIC = {
        "value": 1,
        "getName": function () {
            return "Automatic";
        },
        "getDisplay": function () {
            return "form.text.backOffice.awardingMethodType.AUTOMATIC";
        }
    };


    (function () {
        for (atr in AwardingMethodType) {
            var obj = AwardingMethodType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AwardingMethodType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AwardingMethodType.values = function () {
        return objects;
    };
})();

if (typeof (CalculationType) == 'undefined') {
    CalculationType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CalculationType.NONE = {
        "value": 0,
        "getName": function () {
            return "None";
        }
    };
//	CalculationType.FIRSTDEPOSIT = {
//		"value" : 1,
//		"getName" : function() { return "First Deposit Bonus"; }
//	};
    CalculationType.FRESHFOUNDS = {
        "value": 1,
        "getName": function () {
            return "Fresh Founds";
        }
    };
    CalculationType.ONESINGLE = {
        "value": 2,
        "getName": function () {
            return "One Single Bet";
        }
    };
    CalculationType.ACCUMULATE = {
        "value": 3,
        "getName": function () {
            return "Accumulated";
        }
    };


    (function () {
        for (atr in CalculationType) {
            var obj = CalculationType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CalculationType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CalculationType.values = function () {
        return objects;
    };
})();

if (typeof (PeriodType) == 'undefined') {
    PeriodType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PeriodType.DAILY = {
        "value": 1,
        "getName": function () {
            return "Daily";
        }
    };
    PeriodType.WEEKLY = {
        "value": 2,
        "getName": function () {
            return "Weekly";
        }
    };
    PeriodType.MONTHLY = {
        "value": 3,
        "getName": function () {
            return "Monthly";
        }
    };


    (function () {
        for (atr in PeriodType) {
            var obj = PeriodType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PeriodType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PeriodType.values = function () {
        return objects;
    };
})();


if (typeof (AmountType) == 'undefined') {
    AmountType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AmountType.PERCENTAGE = {
        "value": 0,
        "getName": function () {
            return "Percentage";
        },
        "getFullName": function () {
            return "Percentage";
        }
    };
    AmountType.FIXED = {
        "value": 1,
        "getName": function () {
            return "Fixed amount";
        },
        "getFullName": function () {
            return "Fixed amount";
        }
    };


    (function () {
        for (atr in AmountType) {
            var obj = AmountType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AmountType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AmountType.values = function () {
        return objects;
    };
})();

if (typeof (AccountBonusTurnoverStatusType) == 'undefined') {
    AccountBonusTurnoverStatusType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AccountBonusTurnoverStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Inactive";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.inactive";
        },
    };
    AccountBonusTurnoverStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-primary";
        },
        "getName": function () {
            return "Active";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.active";
        },
    };
    AccountBonusTurnoverStatusType.COMPLETE = {
        "value": 2,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Complete";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.complete";
        },
    };
    AccountBonusTurnoverStatusType.RETURN_TO_MAIN = {
        "value": 3,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Return to main";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.returnToMain";
        },
    };
    AccountBonusTurnoverStatusType.SUSPEND = {
        "value": -1,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Suspend";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.suspend";
        },
    };
    AccountBonusTurnoverStatusType.EXPIRED = {
        "value": -2,
        "getClassName": function () {
            return "label-info";
        },
        "getName": function () {
            return "Expired";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.expired";
        },
    };
    AccountBonusTurnoverStatusType.AUTO_FORCE_SERVE = {
        "value": -3,
        "getClassName": function () {
            return "label-info";
        },
        "getName": function () {
            return "Auto force serve";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.autoForceServe";
        },
    };
    AccountBonusTurnoverStatusType.MANUAL_FORCE_SERVE = {
        "value": -4,
        "getClassName": function () {
            return "label-info";
        },
        "getName": function () {
            return "Manual force serve";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.status.manualForceServe";
        },
    };


    (function () {
        for (atr in AccountBonusTurnoverStatusType) {
            var obj = AccountBonusTurnoverStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AccountBonusTurnoverStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AccountBonusTurnoverStatusType.values = function () {
        return objects;
    };
})();

if (typeof (PromotionPurposeType) == 'undefined') {
    PromotionPurposeType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PromotionPurposeType.ACQUISITION = {
        "value": 0,
        "getName": function () {
            return "Acquisition";
        },
        "getDisplay": function () {
            return "form.text.backOffice.promotionPurposeType.ACQUISITION";
        }
    };
    PromotionPurposeType.CONVERSION = {
        "value": 1,
        "getName": function () {
            return "Conversion";
        },
        "getDisplay": function () {
            return "form.text.backOffice.promotionPurposeType.CONVERSION";
        }
    };
    PromotionPurposeType.RETENTION = {
        "value": 2,
        "getName": function () {
            return "Retention";
        },
        "getDisplay": function () {
            return "form.text.backOffice.promotionPurposeType.RETENTION";
        }
    };
    PromotionPurposeType.REACTIVATION = {
        "value": 3,
        "getName": function () {
            return "Reactivation";
        },
        "getDisplay": function () {
            return "form.text.backOffice.promotionPurposeType.REACTIVATION";
        }
    };


    (function () {
        for (atr in PromotionPurposeType) {
            var obj = PromotionPurposeType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PromotionPurposeType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PromotionPurposeType.values = function () {
        return objects;
    };
})();

if (typeof (BonusCategoryType) == 'undefined') {
    BonusCategoryType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    BonusCategoryType.NORMAL = {
        "value": 0,
        "getName": function () {
            return "Normal";
        }
    };
    BonusCategoryType.FDB = {
        "value": 1,
        "getName": function () {
            return "FDB";
        }
    };
    BonusCategoryType.REBATE = {
        "value": 2,
        "getName": function () {
            return "Rebate";
        }
    };
    BonusCategoryType.CASHBACK = {
        "value": 3,
        "getName": function () {
            return "Cashback";
        }
    };
    BonusCategoryType.RELOAD_BONUS = {
        "value": 4,
        "getName": function () {
            return "Reload";
        }
    };
    BonusCategoryType.REALTIME_REBATE = {
        "value": 5,
        "getName": function () {
            return "Real Time Rebate";
        }
    };
    BonusCategoryType.BIRTHDAY = {
        "value": 6,
        "getName": function () {
            return "Birthday";
        }
    };
    BonusCategoryType.KYC = {
        "value": 7,
        "getName": function () {
            return "KYC";
        }
    };
    BonusCategoryType.REGISTRATION = {
        "value": 9,
        "getName": function () {
            return "Registration";
        }
    };
    BonusCategoryType.FREE_CREDIT = {
        "value": 10,
        "getName": function () {
            return "Free Credit";
        }
    };
    BonusCategoryType.SPIN_AND_WIN = {
        "value": 11,
        "getName": function () {
            return "Spin&Win";
        }
    };
    BonusCategoryType.SYSTEM = {
        "value": 99,
        "getName": function () {
            return "System";
        }
    };


    (function () {
        for (atr in BonusCategoryType) {
            var obj = BonusCategoryType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BonusCategoryType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BonusCategoryType.values = function () {
        return objects;
    };
})();

if (typeof (PeriodType) == 'undefined') {
    PeriodType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PeriodType.INDEFINITE = {
        "value": 0,
        "getName": function () {
            return "Indefinite";
        }
    };
    PeriodType.DAILY = {
        "value": 1,
        "getName": function () {
            return "Daily";
        }
    };
    PeriodType.WEEKLY = {
        "value": 2,
        "getName": function () {
            return "Weekly";
        }
    };
    PeriodType.MONTHLY = {
        "value": 3,
        "getName": function () {
            return "Monthly";
        }
    };


    (function () {
        for (atr in PeriodType) {
            var obj = PeriodType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PeriodType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PeriodType.values = function () {
        return objects;
    };
})();

if (typeof (DBOrderType) == 'undefined') {
    DBOrderType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    DBOrderType.ASC = {
        "value": 0
    };
    DBOrderType.DESC = {
        "value": 1
    };


    (function () {
        for (atr in DBOrderType) {
            var obj = DBOrderType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    DBOrderType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    DBOrderType.values = function () {
        return objects;
    };
})();

if (typeof (PlatformType) == 'undefined') {
    PlatformType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PlatformType.WEB = {
        "value": 1,
        "getName": function () {
            return "Web";
        },
        "getFieldName": function () {
            return "WebMessageContent";
        }
    };
    PlatformType.HTML5 = {
        "value": 2,
        "getName": function () {
            return "Html5";
        },
        "getFieldName": function () {
            return "Html5MessageContent";
        }
    };
    PlatformType.APP = {
        "value": 4,
        "getName": function () {
            return "App";
        },
        "getFieldName": function () {
            return "AppMessageContent";
        }
    };


    (function () {
        for (atr in PlatformType) {
            var obj = PlatformType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PlatformType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PlatformType.values = function () {
        return objects;
    };
})();

if (typeof (AccountStatusType) == 'undefined') {
    AccountStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AccountStatusType.INACTIVED = {
        "value": 0,
        "getClassName": function () {
            return "label-default";
        },
        "getAffiliateClassName": function () {
            return "state-inactive";
        },
        "getName": function () {
            return "INACTIVED";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.inactive";
        },
    };
    AccountStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getAffiliateClassName": function () {
            return "state-active";
        },
        "getName": function () {
            return "ACTIVE";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.active";
        },
    };
    AccountStatusType.SUSPEND = {
        "value": 2,
        "getClassName": function () {
            return "label-warning";
        },
        "getAffiliateClassName": function () {
            return "state-suspend";
        },
        "getName": function () {
            return "SUSPEND";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.suspend";
        },
    };
    AccountStatusType.LOCKED = {
        "value": 3,
        "getClassName": function () {
            return "label-danger";
        },
        "getAffiliateClassName": function () {
            return "state-locked";
        },
        "getName": function () {
            return "LOCKED";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.locked";
        },
    };

    (function () {
        for (atr in AccountStatusType) {
            var obj = AccountStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AccountStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();

if (typeof (ContactVerifiedType) == 'undefined') {
    ContactVerifiedType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ContactVerifiedType.Email = {
        "value": 1
    };
    ContactVerifiedType.Phone = {
        "value": 2
    };

    (function () {
        for (atr in ContactVerifiedType) {
            var obj = ContactVerifiedType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ContactVerifiedType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();

if (typeof (ContactType) == 'undefined') {
    ContactType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ContactType.Email = {
        "value": 1,
        "displayName": "fe.text.profile.contactType.email",
        "imageName": "email.svg",
        "getName": function () {
            return "email";
        },
        "isNeedCallingCode": false,
        "idName": "email"
    };
    ContactType.Phone = {
        "value": 2,
        "displayName": "fe.text.profile.contactType.phone",
        "imageName": "phone.svg",
        "getName": function () {
            return "phoneNumber";
        },
        "isNeedCallingCode": true,
        "idName": "phone"
    };

    (function () {
        for (atr in ContactType) {
            var obj = ContactType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ContactType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ContactType.values = function () {
        return objects;
    };
})();

if (typeof (WithdrawalRestrictionType) == 'undefined') {
    WithdrawalRestrictionType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    WithdrawalRestrictionType.PERSONAL_INFO = {
        "value": 1,
        "getName": function () {
            return "personalInfo";
        },
    };
    WithdrawalRestrictionType.CONTACT = {
        "value": 2,
        "getName": function () {
            return "contact";
        },
    };
    WithdrawalRestrictionType.DOCUMENT = {
        "value": 3,
        "getName": function () {
            return "document";
        },
    };
    WithdrawalRestrictionType.OTHER = {
        "value": 99,
        "getName": function () {
            return "other";
        },
    };

    (function () {
        for (atr in WithdrawalRestrictionType) {
            var obj = WithdrawalRestrictionType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WithdrawalRestrictionType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    WithdrawalRestrictionType.values = function () {
        return objects;
    };
})();


if (typeof (LoginType) == 'undefined') {
    LoginType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    LoginType.Account = {
        "value": 0,
        "displayName": "fe.text.profile.contactType.email",
        "imageName": "account.svg",
        "getName": function () {
            return "account";
        },
        "isNeedCallingCode": false,
    };
    LoginType.Phone = {
        "value": 1,
        "displayName": "fe.text.profile.contactType.phone",
        "imageName": "phone.svg",
        "getName": function () {
            return "phoneNumber";
        },
        "isNeedCallingCode": true,
    };
    LoginType.Email = {
        "value": 2,
        "displayName": "fe.text.profile.contactType.email",
        "imageName": "email.svg",
        "getName": function () {
            return "email";
        },
        "isNeedCallingCode": false,
    };
    LoginType.Facebook = {
        "value": 3,
        "displayName": "form.text.contactType.facebook",
        "imageName": "facebook.svg",
        "getName": function () {
            return "fbMessengerId";
        },
        "isNeedCallingCode": false,
    };
    LoginType.Google = {
        "value": 4,
        "displayName": "fe.text.profile.contactType.google",
        "imageName": "google.svg",
        "getName": function () {
            return "googleId";
        },
        "isNeedCallingCode": false,
    };

    (function () {
        for (atr in LoginType) {
            var obj = LoginType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    LoginType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    LoginType.values = function () {
        return objects;
    };
})();

if (typeof (WebsiteSystemSettingType) == 'undefined') {
    WebsiteSystemSettingType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    WebsiteSystemSettingType.FEATURE_GAME = {
        "value": 1
    };
    WebsiteSystemSettingType.WEBSITE_STATUS = {
        "value": 2
    };
    WebsiteSystemSettingType.MAINTAIN_START_DATE = {
        "value": 3
    };
    WebsiteSystemSettingType.MAINTAIN_END_DATE = {
        "value": 4
    };
    WebsiteSystemSettingType.PG_PROXY_PRIORITY = {
        "value": 5
    };
    WebsiteSystemSettingType.PG_MAX_FAILED = {
        "value": 6
    };
    WebsiteSystemSettingType.PG_TEST_ACCOUNT = {
        "value": 7
    };
    WebsiteSystemSettingType.CS_LIVE_CHAT = {
        "value": 8
    };
    WebsiteSystemSettingType.BAN_COUNTRIES = {
        "value": 9
    };
    WebsiteSystemSettingType.BO_DOMAIN = {
        "value": 10
    };
    WebsiteSystemSettingType.REGISTER_SUCCESS_MESSAGE = {
        "value": 11
    };
    WebsiteSystemSettingType.BO_AFFILIATE_DOMAIN = {
        "value": 12
    };
    WebsiteSystemSettingType.DEFAULT_BONUS_TEMPLATE = {
        "value": 13
    };
    WebsiteSystemSettingType.CS_MAIL = {
        "value": 14
    };
    WebsiteSystemSettingType.CS_QQ = {
        "value": 15
    };
    WebsiteSystemSettingType.CS_WECHAT = {
        "value": 16
    };
    WebsiteSystemSettingType.APP_VERSION = {
        "value": 17
    };
    WebsiteSystemSettingType.MIN_DEPOSIT_AMOUNT = {
        "value": 18
    };
    WebsiteSystemSettingType.MIN_WITHDRAW_AMOUNT = {
        "value": 19
    };
    WebsiteSystemSettingType.MIN_FORCE_SERVE = {
        "value": 20
    };
    WebsiteSystemSettingType.DEPOSIT_SETTING_ORDER = {
        "value": 21
    };
    WebsiteSystemSettingType.RANKING_TYPE = {
        "value": 22
    };
    WebsiteSystemSettingType.ALL_PAGE_SEO = {
        "value": 24
    };
    WebsiteSystemSettingType.DEPOSIT_ALLOW_PENDING_PAYMENT = {
        "value": 25
    };
    WebsiteSystemSettingType.RESTRICT_DUPLICATE_BANKACCOUNTS = {
        "value": 26
    };
    WebsiteSystemSettingType.BANK_AMOUNT_LIMIT = {
        "value": 27
    };
    WebsiteSystemSettingType.DEPOSIT_METHOD_ACTIVE_SETTING = {
        "value": 28
    };
    WebsiteSystemSettingType.WITHDRAWAL_SETTING_ORDER = {
        "value": 29
    };
    WebsiteSystemSettingType.WITHDRAWAL_METHOD_ACTIVE_SETTING = {
        "value": 30
    };
    WebsiteSystemSettingType.WITHDRAWAL_ALLOW_PENDING_PAYMENT = {
        "value": 31
    };
    WebsiteSystemSettingType.WITHDRAWAL_RESTRICTION = {
        "value": 32
    };
    WebsiteSystemSettingType.WITHDRAWAL_OTP = {
        "value": 33
    };
    WebsiteSystemSettingType.VERIFY_DOCUMENT = {
        "value": 34
    };
    WebsiteSystemSettingType.VERIFY_BANK = {
        "value": 35
    };
    WebsiteSystemSettingType.WITHDRAWAL_REVERT = {
        "value": 36
    };
    WebsiteSystemSettingType.DEPOSIT_ALLOW_PENDING_PAYMENT_AFTER_PG = {
        "value": 37
    };
    WebsiteSystemSettingType.UPLOAD_VERIFY_BANK_SECOND_PHOTO = {
        "value": 38
    };
    WebsiteSystemSettingType.FORGET_PASSWORD_FUNCTION_TIME_LIMIT = {
        "value": 39
    };
    WebsiteSystemSettingType.FORGET_PASSWORD_SEND_TIME_LIMIT = {
        "value": 40
    };
    WebsiteSystemSettingType.AFFILIATE_WITHDRAWAL_RESTRICTION = {
        "value": 44
    };
    WebsiteSystemSettingType.CLOSE_AUTO_VERIFY_WITHDRAWAL = {
        "value": 46
    };
    WebsiteSystemSettingType.ALLOW_VIP_EX = {
        "value": 47
    };
    WebsiteSystemSettingType.DIRECTLY_VERIFY_PHONE_AFTER_ADDING = {
        "value": 48
    };
    WebsiteSystemSettingType.ONLY_ALREADY_VERIFIED_TO_WITHDRAW = {
        "value": 49
    };
    WebsiteSystemSettingType.VERIFY_DOCUMENT_PAGE = {
        "value": 50
    };
    WebsiteSystemSettingType.ENABLE_MULTIPLE_REGISTER_AND_LOGIN_FEATURES = {
        "value": 53
    };
    WebsiteSystemSettingType.ENABLE_FAST_REGISTER_AND_LOGIN = {
        "value": 54
    };
    WebsiteSystemSettingType.ALLOW_FORCE_SERVE = {
        "value": 57
    };
    WebsiteSystemSettingType.NEED_OTP_VERIFICATION_TO_WITHDRAW_TO_PLAYER = {
        "value": 58
    }
    WebsiteSystemSettingType.WITHDRAWAL_RESTRICTED_ACTIVE_PLAYER = {
        "value": 71
    };
    WebsiteSystemSettingType.SPIN_AND_WIN_SETTING = {
        "value": 76
    };

    (function () {
        for (atr in WebsiteSystemSettingType) {
            var obj = WebsiteSystemSettingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WebsiteSystemSettingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();

if (typeof (WebsiteLoginSettingType) == 'undefined') {
    WebsiteLoginSettingType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    WebsiteLoginSettingType.PHONE = {
        "value": 1,
        "name": "PHONE"
    };
    WebsiteLoginSettingType.EMAIL = {
        "value": 2,
        "name": "EMAIL"
    };
    WebsiteLoginSettingType.FACEBOOK = {
        "value": 3,
        "name": "FACEBOOK"
    };
    WebsiteLoginSettingType.GOOGLE = {
        "value": 4,
        "name": "FACEBOOK"
    };

    (function () {
        for (atr in WebsiteLoginSettingType) {
            var obj = WebsiteLoginSettingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WebsiteLoginSettingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();


if (typeof (UserType) == 'undefined') {
    UserType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    UserType.COMPANY = {
        "value": 5
    };
    UserType.HOUSE = {
        "value": 4
    };
    UserType.MANAGER = {
        "value": 3
    };
    UserType.MASTER_AGENT = {
        "value": 2
    };
    UserType.AGENT = {
        "value": 1
    };
    UserType.PLAYER = {
        "value": 0
    };

    (function () {
        for (atr in UserType) {
            var obj = UserType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    UserType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();

if (typeof (UserLevel) == 'undefined') {
    UserLevel = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    UserLevel.COMPANY = {
        "value": 0
    };
    UserLevel.HOUSE = {
        "value": 1
    };
    UserLevel.MANAGER = {
        "value": 2
    };
    UserLevel.MASTER_AGENT = {
        "value": 3
    };
    UserLevel.AGENT = {
        "value": 4
    };
    UserLevel.PLAYER = {
        "value": 5
    };

    (function () {
        for (atr in UserLevel) {
            var obj = UserLevel[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    UserLevel.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();


if (typeof (CashTransferType) == 'undefined') {
    CashTransferType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CashTransferType.Deposit = {
        "value": 0
    };
    CashTransferType.Withdraw = {
        "value": 1
    };

    (function () {
        for (atr in CashTransferType) {
            var obj = CashTransferType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CashTransferType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };
})();

if (typeof (PlatformGameType) == 'undefined') {
    PlatformGameType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PlatformGameType.JDB_CASINO = {
        "gameName": "JDB Casino"
    };
    PlatformGameType.JDB_SLOT = {
        "gameName": "JDB Slot"
    };
    PlatformGameType.SV388_LIVE = {
        "gameName": "Cock Fighting"
    };
    PlatformGameType.CITI_LIVE = {
        "gameName": "Horse Racing"
    };
    PlatformGameType.AG_LIVE = {
        "gameName": "AG Casino"
    };
    PlatformGameType.MG_LIVE = {
        "gameName": "Micro Gaming"
    };
    PlatformGameType.FH_FH = {
        "gameName": "Fish Hunting"
    };
    PlatformGameType.LOTTERY3D_LIVE = {
        "gameName": "Lottery 3D"
    };
    PlatformGameType.ALLBET_LIVE = {
        "gameName": "ALLBET"
    };
    PlatformGameType.GOLDENRACE_LIVE = {
        "gameName": "Golden Race"
    };

    (function () {
        for (atr in PlatformGameType) {
            var obj = PlatformGameType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PlatformGameType.getInstanceByName = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["name"] == value) {
                return objects[i];
            }
        }
        return "";
    };
})();

if (typeof (PGAccountStatusType) == 'undefined') {
    PGAccountStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PGAccountStatusType.INACTIVE = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.inactive";
        },
    };
    PGAccountStatusType.MAINTENANCE = {
        "value": 0,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Maintain";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.maintenance";
        },
    };
    PGAccountStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        },
        "getFullName": function () {
            return "form.text.backOffice.status.active";
        },
    };


    (function () {
        for (atr in PGAccountStatusType) {
            var obj = PGAccountStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PGAccountStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PGAccountStatusType.values = function () {
        return objects;
    };
})();

if (typeof (PaymentDisplaySettingStatusType) == 'undefined') {
    PaymentDisplaySettingStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PaymentDisplaySettingStatusType.INACTIVE = {
        "value": 0,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Inactive";
        }
    };
    PaymentDisplaySettingStatusType.ACTIVE = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in PaymentDisplaySettingStatusType) {
            var obj = PaymentDisplaySettingStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PaymentDisplaySettingStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PaymentDisplaySettingStatusType.values = function () {
        return objects;
    };
})();

if (typeof (NotificationSettingType) == 'undefined') {
    NotificationSettingType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    NotificationSettingType.DEPOSIT = {
        "value": 0,
        "getName": function () {
            return "Deposit";
        }
    };
    NotificationSettingType.WITHDRAWAL = {
        "value": 1,
        "getName": function () {
            return "Withdrawal";
        }
    };
    NotificationSettingType.PLAYER = {
        "value": 2,
        "getName": function () {
            return "Player";
        }
    };


    (function () {
        for (atr in NotificationSettingType) {
            var obj = NotificationSettingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    NotificationSettingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    NotificationSettingType.values = function () {
        return objects;
    };
})();


if (typeof (AffiliateType) == 'undefined') {
    AffiliateType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AffiliateType.INACTIVE = {
        "value": 0,
        "getName": function () {
            return "Inactive";
        }
    };
    AffiliateType.ACTIVE = {
        "value": 1,
        "getName": function () {
            return "Active";
        }
    };


    (function () {
        for (atr in AffiliateType) {
            var obj = AffiliateType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AffiliateType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AffiliateType.values = function () {
        return objects;
    };
})();

if (typeof (AffiliateApplicationType) == 'undefined') {
    AffiliateApplicationType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AffiliateApplicationType.REGISTRATIONS = {
        "value": 0,
        "getName": function () {
            return "Registrations";
        }
    };
    AffiliateApplicationType.WITHDRAWALS = {
        "value": 1,
        "getName": function () {
            return "Withdrawals";
        }
    };
    AffiliateApplicationType.CONTACT_INFO = {
        "value": 2,
        "getName": function () {
            return "Contact Info";
        }
    };
    AffiliateApplicationType.BANK_INFO = {
        "value": 3,
        "getName": function () {
            return "Bank Info";
        }
    };


    (function () {
        for (atr in AffiliateType) {
            var obj = AffiliateType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AffiliateApplicationType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AffiliateApplicationType.values = function () {
        return objects;
    };
})();

if (typeof (DocumentType) == 'undefined') {
    DocumentType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];
    var objectIds_basic = [1, 4, 8, 16, 3, 5, 6];
    var objects_basic = [];
    var objectIds_bdt = [1, 4, 8, 16, 3, 5, 6, 7];
    var objects_bdt = [];
    var objectIds_inr = [1, 4, 8, 16, 32, 3, 5, 6, 13];
    var objects_inr = [];
    var objectIds_php = [2, 4, 8, 16, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 3, 5, 6];
    var objects_php = [];
    var objectIds_myr = [1, 4, 8, 16, 12];
    var objects_myr = [];

    DocumentType.ID = {
        "value": 1,
        "getName": function () {
            return "id";
        },
        "getFullName": function () {
            return "fe.text.documentType.id";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.HEADSHOT = {
        "value": 2,
        "getName": function () {
            return "headshot";
        },
        "getFullName": function () {
            return "fe.text.documentType.headshot";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.PASSPORT = {
        "value": 4,
        "getName": function () {
            return "passport";
        },
        "getFullName": function () {
            return "fe.text.documentType.passport";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.DRIVER_LICENSE = {
        "value": 8,
        "getName": function () {
            return "driver license";
        },
        "getFullName": function () {
            return "fe.text.documentType.driverLicense";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.BANK_STATEMENT = {
        "value": 16,
        "getName": function () {
            return "bank card";
        },
        "getFullName": function () {
            return "fe.text.documentType.bankStatement";
        },
        "getGroupType": function () {
            return DocumentGroupType.BANK.value;
        }
    };
    DocumentType.PAN_CARD = {
        "value": 32,
        "getName": function () {
            return "pan card";
        },
        "getFullName": function () {
            return "fe.text.documentType.panCard";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.GSIS_ID = {
        "value": 64,
        "getName": function () {
            return "gsis id";
        },
        "getFullName": function () {
            return "fe.text.documentType.gsisId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.SCHOOL_ID = {
        "value": 128,
        "getName": function () {
            return "school id";
        },
        "getFullName": function () {
            return "fe.text.documentType.schoolId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.PRC_ID = {
        "value": 256,
        "getName": function () {
            return "pan card";
        },
        "getFullName": function () {
            return "fe.text.documentType.prcId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.VOTERS_ID = {
        "value": 512,
        "getName": function () {
            return "voters id";
        },
        "getFullName": function () {
            return "fe.text.documentType.votersId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.SENIOR_CITIZEN_ID = {
        "value": 1024,
        "getName": function () {
            return "senior citizen id";
        },
        "getFullName": function () {
            return "fe.text.documentType.seniorCitizenId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.UNIFIED_MULTI_PURPOSE_ID = {
        "value": 2048,
        "getName": function () {
            return "unified multi purpose id";
        },
        "getFullName": function () {
            return "fe.text.documentType.unifiedMultiPurposeId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.BARANGAY_CERTIFICATE = {
        "value": 4096,
        "getName": function () {
            return "barangay certificate";
        },
        "getFullName": function () {
            return "fe.text.documentType.barangayCertificate";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.FOREIGN_CERTIFICATE_OF_REGISTRATION = {
        "value": 8192,
        "getName": function () {
            return "foreign certificate of registration";
        },
        "getFullName": function () {
            return "fe.text.documentType.foreignCertificateOfRegistration";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.SOCIAL_SECURITY_SYSTEM_ID = {
        "value": 16384,
        "getName": function () {
            return "social security system id";
        },
        "getFullName": function () {
            return "fe.text.documentType.socialSecuritySystemId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.TIN_CARD = {
        "value": 32768,
        "getName": function () {
            return "tin card";
        },
        "getFullName": function () {
            return "fe.text.documentType.tinCard";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.POSTAL_ID = {
        "value": 65536,
        "getName": function () {
            return "postal id";
        },
        "getFullName": function () {
            return "fe.text.documentType.postalId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.PAG_IBIG_LOYALTY_ID = {
        "value": 131072,
        "getName": function () {
            return "pag-ibig loyalty id";
        },
        "getFullName": function () {
            return "fe.text.documentType.pagIbigLoyaltyId";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.PHILHEALTH_CARD = {
        "value": 262144,
        "getName": function () {
            return "foreign certificate of registration";
        },
        "getFullName": function () {
            return "fe.text.documentType.philHealthCard";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.CREDIT_CARD_STATEMENT = {
        "value": 524288,
        "getName": function () {
            return "credit card statement";
        },
        "getFullName": function () {
            return "fe.text.documentType.credit_card_statement";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.UTILITIES_BILL = {
        "value": 1048576,
        "getName": function () {
            return "utilities bill";
        },
        "getFullName": function () {
            return "fe.text.documentType.utilities_bill";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };
    DocumentType.SURECASH = {
        "value": 7,
        "getName": function () {
            return "Sure Cash";
        },
        "getFullName": function () {
            return "fe.text.documentType.sureCash";
        },
        "getGroupType": function () {
            return DocumentGroupType.EWALLET.value;
        }
    };
    DocumentType.CPF = {
        "value": 9,
        "getName": function () {
            return "CPF";
        },
        "getFullName": function () {
            return "fe.text.documentType.cpf";
        },
        "getGroupType": function () {
            return DocumentGroupType.DOCUMENT.value;
        }
    };

    DocumentType.UPI = {
        "value": 13,
        "getName": function () {
            return "UPI";
        },
        "getFullName": function () {
            return "fe.text.documentType.upi";
        },
        "getGroupType": function () {
            return DocumentGroupType.UPI.value;
        }
    };

    (function () {
        for (atr in DocumentType) {
            var obj = DocumentType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
            if (objectIds_basic.includes(obj.value)) {
                objects_basic[objects_basic.length] = obj;
            }
            if (objectIds_inr.includes(obj.value)) {
                objects_inr[objects_inr.length] = obj;
            }
            if (objectIds_bdt.includes(obj.value)) {
                objects_bdt[objects_bdt.length] = obj;
            }
            if (objectIds_php.includes(obj.value)) {
                objects_php[objects_php.length] = obj;
            }
            if (objectIds_myr.includes(obj.value)) {
                objects_myr[objects_myr.length] = obj;
            }
        }
    })();

    DocumentType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    DocumentType.getValuesByCurrency = function (currencyIds) {
        let objects = [];
        for (let currencyId of currencyIds.values()) {
            if (currencyId === CurrencyType.INR.value) {
                objects = [...new Set([...objects, ...objects_inr])];
            } else if (currencyId === CurrencyType.BDT.value) {
                objects = [...new Set([...objects, ...objects_bdt])];
            } else if (currencyId === CurrencyType.PHP.value) {
                objects = [...new Set([...objects, ...objects_php])];
            } else if (currencyId === CurrencyType.MYR.value) {
                objects = [...new Set([...objects, ...objects_myr])];
            } else {
                objects = [...new Set([...objects, ...objects_basic])];
            }
        }

        return objects;
    };

    DocumentType.getAffiliateAddressType = function () {
        return [DocumentType.BANK_STATEMENT.value, DocumentType.CREDIT_CARD_STATEMENT.value, DocumentType.UTILITIES_BILL.value]
    }

    DocumentType.values = function () {
        return objects;
    };

    DocumentType.getFrontFieldName = function () {
        return "documentFrontImage";
    };

    DocumentType.getBackFieldName = function () {
        return "documentBackImage";
    };

    DocumentType.getOtherFieldName = function () {
        return "documentOtherImage";
    };

})();


/*
 *
 */
if (typeof (BankType) == 'undefined') {
    BankType = {};
}

(function () {
    const getValue = function () {
        return this.value;
    };
    const getLevel = function () {
        return this.level;
    };
    const getWithdrawalContact = function () {
        return this.withdrawalContact;
    };
    const getWithdrawalDocument = function () {
        return this.withdrawalDocument;
    };
    const isNeedPhoneVerify = function () {
        return (this.withdrawalContact & ContactType.Phone.unique()) === ContactType.Phone.unique();
    };
    const isNeedEmailVerify = function () {
        return (this.withdrawalContact & ContactType.Email.unique()) === ContactType.Email.unique();
    };
    const isNeedCPFVerify = function () {
        return (this.withdrawalDocument === DocumentType.CPF.unique())
    };
    const objects = [];

    BankType.LOCAL_BANK = {
        "value": 0,
        "category": "Bank Card",
        "level": 1,
        "withdrawalContact": 0,
        "withdrawalDocument": 0
    };
    BankType.ONLINE_BANKING = {
        "value": 1,
        "category": "Bank Card",
        "level": 0,
        "withdrawalContact": 0,
        "withdrawalDocument": 0
    };
    BankType.CREDITCARD = {
        "value": 40,
        "category": "CREDITCARD",
        "level": 0,
        "withdrawalContact": 0,
        "withdrawalDocument": 0
    };

    (function () {
        for (atr in BankType) {
            var obj = BankType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
            obj.getLevel = getLevel;
            obj.isNeedPhoneVerify = isNeedPhoneVerify;
            obj.isNeedEmailVerify = isNeedEmailVerify;
            obj.isNeedCPFVerify = isNeedCPFVerify;
        }
    })();

    BankType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BankType.values = function () {
        return objects;
    };

})();

/*
 *
 */
if (typeof (PaymentType) == 'undefined') {
    PaymentType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PaymentType.NOT_FOUND = {
        "value": -1,
        "name": "Not Found"
    };
    PaymentType.LOCAL_BANKING = {
        "value": 0,
        "name": "LOCAL BANKING"
    };
    PaymentType.ONLINE_BANKING = {
        "value": 1,
        "name": "ONLINE BANKING"
    };
    PaymentType.ALIPAYSCAN = {
        "value": 2,
        "name": "ALIPAYSCAN"
    };
    PaymentType.ALIPAYWAP = {
        "value": 3,
        "name": "ALIPAYWAP"
    };
    PaymentType.ALIPAYH5 = {
        "value": 4,
        "name": "ALIPAYH5"
    };
    PaymentType.WECHATSCAN = {
        "value": 5,
        "name": "WECHATSCAN"
    };
    PaymentType.WECHATWAP = {
        "value": 6,
        "name": "WECHATWAP"
    };
    PaymentType.WECHATH5 = {
        "value": 7,
        "name": "WECHATH5"
    };
    PaymentType.JINGDONGSCAN = {
        "value": 8,
        "name": "JINGDONGSCAN"
    };
    PaymentType.JINGDONGWAP = {
        "value": 9,
        "name": "JINGDONGWAP"
    };
    PaymentType.JINGDONGH5 = {
        "value": 10,
        "name": "JINGDONGH5"
    };
    PaymentType.QQPAYSCAN = {
        "value": 11,
        "name": "QQPAYSCAN"
    };
    PaymentType.QQPAYWAP = {
        "value": 12,
        "name": "QQPAYWAP"
    };
    PaymentType.QQPAYH5 = {
        "value": 13,
        "name": "QQPAYH5"
    };
    PaymentType.UNIONPAYSCAN = {
        "value": 14,
        "name": "UNIONPAYSCAN"
    };
    PaymentType.BAIDUSCAN = {
        "value": 15,
        "name": "BAIDUSCAN"
    };
    PaymentType.QUICKPAY = {
        "value": 16,
        "name": "QUICKPAY"
    };
    PaymentType.TENPAYWAP = {
        "value": 18,
        "name": "TENPAYWAP"
    };
    PaymentType.TENPAYSCAN = {
        "value": 19,
        "name": "TENPAYSCAN"
    };
    PaymentType.BAIDUWAP = {
        "value": 20,
        "name": "BAIDUWAP"
    };
    PaymentType.IFPAYSCAN = {
        "value": 22,
        "name": "IFPAYSCAN"
    };
    PaymentType.IFPAYWAP = {
        "value": 23,
        "name": "IFPAYWAP"
    };
    PaymentType.ALIPAYTRANSFER = {
        "value": 34,
        "name": "ALIPAYTRANSFER"
    };
    PaymentType.WECHATTRANSFER = {
        "value": 35,
        "name": "WECHATTRANSFER"
    };
//	PaymentType.QUICKPASS = {
//		"value" : 36,
//		"name" : "QUICKPASS" // cloud quick pass
//	};
//	PaymentType.QUICKPASSSCAN = {
//		"value" : 37,
//		"name" : "QUICKPASSSCAN" // cloud quick pass scan
//	};
    PaymentType.QUICKPASSWAP = {
        "value": 38,
        "name": "QUICKPASSWAP"
    };
    PaymentType.QQTRANSFER = {
        "value": 42,
        "name": "QQTRANSFER"
    };
    PaymentType.MOMOPAYTRANSFER = {
        "value": 43,
        "name": "MOMOPAYTRANSFER"
    };
    PaymentType.BKASHWALLET = {
        "value": 44,
        "name": "BKASH"
    };
    PaymentType.ROCKETWALLET = {
        "value": 45,
        "name": "ROCKET"
    };
    PaymentType.NAGADWALLET = {
        "value": 46,
        "name": "NAGAD"
    };
    PaymentType.BKASHTRANSFER = {
        "value": 47,
        "name": "BKASHTRANSFER",
        'usePhoneAsBankAccountNo': true
    };
    PaymentType.ROCKETTRANSFER = {
        "value": 48,
        "name": "ROCKETTRANSFER",
        'usePhoneAsBankAccountNo': true
    };
    PaymentType.NAGADTRANSFER = {
        "value": 49,
        "name": "NAGADTRANSFER",
        'usePhoneAsBankAccountNo': true
    };
    PaymentType.MOMOPAYSCAN = {
        "value": 50,
        "name": "MOMOPAYSCAN"
    };
    PaymentType.ZALOSCAN = {
        "value": 51,
        "name": "ZALOSCAN"
    };
    PaymentType.UPI = {
        "value": 52,
        "name": "UPI"
    };
    PaymentType.UNIONPAYH5 = {
        "value": 53,
        "name": "UNIONPAYH5"
    };
    PaymentType.BAIDUH5 = {
        "value": 54,
        "name": "BAIDUH5"
    };
    PaymentType.QUICKPAYH5 = {
        "value": 55,
        "name": "QUICKPAYH5"
    };
    PaymentType.PHONEPEWALLET = {
        "value": 56,
        "name": "PHONEPEWALLET"
    };
    PaymentType.PHONEPETRANSFER = {
        "value": 57,
        "name": "PHONEPETRANSFER",
        'usePhoneAsBankAccountNo': true,
        'usePaymentInfo': true
    };
    PaymentType.EASYPAISAWALLET = {
        "value": 58,
        "name": "EASYPAISAWALLET"
    };
    PaymentType.EASYPAISATRANSFER = {
        "value": 59,
        "name": "EASYPAISATRANSFER",
        'usePhoneAsBankAccountNo': true
    };
    PaymentType.JAZZCASHWALLET = {
        "value": 60,
        "name": "JAZZCASHWALLET"
    };
    PaymentType.JAZZCASHTRANSFER = {
        "value": 61,
        "name": "JAZZCASHTRANSFER",
        'usePhoneAsBankAccountNo': true
    };
    PaymentType.E_SNAPPEDWALLET = {
        "value": 62,
        "name": "E_SNAPPEDWALLET"
    };
    PaymentType.IMPS = {
        "value": 63,
        "name": "IMPS"
    };
    PaymentType.CASHMAALWALLET = {
        "value": 64,
        "name": "CASHMAAL"
    };

    PaymentType.CASHMAALTRANSFER = {
        "value": 65,
        "name": "CASHMAALTRANSFER"
    };

    PaymentType.GCASHWALLET = {
        "value": 66,
        "name": "GCASH"
    };
    PaymentType.GCASHTRANSFER = {
        "value": 67,
        "name": "GCASHTRANSFER"
    };
    PaymentType.GRABPAYWALLET = {
        "value": 68,
        "name": "GRABPAY"
    };
    PaymentType.GRABPAYTRANSFER = {
        "value": 69,
        "name": "GRABPAYTRANSFER"
    };
    PaymentType.USDT_TRC20 = {
        "value": 70,
        "name": "USDT_TRC20",
        "imageName": "trc20.png"
    };
    PaymentType.USDT_ERC20 = {
        "value": 71,
        "name": "USDT_ERC20",
        "imageName": "erc20.png"
    };
    PaymentType.USDT_OMNI = {
        "value": 72,
        "name": "USDT_OMNI",
        "imageName": "omni.png"
    };
    PaymentType.PAYMAYAWALLET = {
        "value": 73,
        "name": "PAYMAYAWALLET"
    };
    PaymentType.PAYMAYATRANSFER = {
        "value": 74,
        "name": "PAYMAYATRANSFER"
    };
    PaymentType.UPAYWALLET = {
        "value": 75,
        "name": "UPAYWALLET"
    };
    PaymentType.UPAYTRANSFER = {
        "value": 76,
        "name": "UPAYTRANSFER",
        'usePhoneAsBankAccountNo': true
    };
    PaymentType.MALDOPAYWALLET = {
        "value": 77,
        "name": "MALDOPAYWALLET"
    };
    PaymentType.TOUCHNGOTRANSFER = {
        "value": 78,
        "name": "TOUCHNGOTRANSFER"
    };
    PaymentType.BOOSTWALLETTRANSFER = {
        "value": 79,
        "name": "BOOSTWALLETTRANSFER"
    };
    PaymentType.USD = {
        "value": 80,
        "name": "USD",
        "imageName": "usd.png"
    };
    PaymentType.EUR = {
        "value": 81,
        "name": "EUR",
        "imageName": "eur.png"
    };
    PaymentType.GOLD = {
        "value": 82,
        "name": "GOLD",
        "imageName": "gold.png"
    };
    PaymentType.GPAYTRANSFER = {
        "value": 83,
        "name": "GPAYTRANSFER",
        'usePaymentInfo': true
    };
    PaymentType.PAYTMTRANSFER = {
        "value": 84,
        "name": "PAYTMTRANSFER",
        'usePaymentInfo': true
    };
    PaymentType.PAYTMWALLET = {
        "value": 85,
        "name": "PAYTMWALLET"
    };
    PaymentType.MOBIKWIK = {
        "value": 86,
        "name": "MOBIKWIK"
    };
    PaymentType.JETONWALLET = {
        "value": 87,
        "name": "JETONWALLET"
    };
    PaymentType.JETONGO = {
        "value": 88,
        "name": "JETONGO"
    };
    PaymentType.FREECHARGE = {
        "value": 89,
        "name": "FREECHARGE"
    };
    PaymentType.FREECHARGE = {
        "value": 90,
        "name": "FREECHARGE"
    };
    PaymentType.NEOSURF = {
        "value": 91,
        "name": "NEOSURF"
    };
    PaymentType.VISACARD = {
        "value": 92,
        "name": "VISACARD"
    };
    PaymentType.AMERICANEXPRESS = {
        "value": 93,
        "name": "AMERICANEXPRESS"
    };
    PaymentType.CARNET = {
        "value": 94,
        "name": "CARNET"
    };
    PaymentType.VIETTELPAYSCAN = {
        "value": 95,
        "name": "VIETTELPAYSCAN"
    };
    PaymentType.TRUSTAXIATAPAYWALLET = {
        "value": 96,
        "name": "TRUSTAXIATAPAYWALLET"
    };
    PaymentType.OKWALLETWALLET = {
        "value": 97,
        "name": "OKWALLETWALLET"
    };
    PaymentType.SURECASHTRANSFER = {
        "value": 98,
        "name": "SURECASHTRANSFER"
    };
    PaymentType.TRUSTAXIATAPAYTRANSFER = {
        "value": 99,
        "name": "TRUSTAXIATAPAYTRANSFER"
    };
    PaymentType.OKWALLETTRANSFER = {
        "value": 100,
        "name": "OKWALLETTRANSFER"
    };
    PaymentType.SURECASHWALLET = {
        "value": 101,
        "name": "SURECASHWALLET"
    };
    PaymentType.CONVENIENCESTORE = {
        "value": 102,
        "name": "CONVENIENCESTORE"
    };
    PaymentType.UPAISAWALLET = {
        "value": 103,
        "name": "UPAISAWALLET"
    };
    PaymentType.UPAISATRANSFER = {
        "value": 104,
        "name": "UPAISATRANSFER",
    };
    PaymentType.UPITRANSFER = {
        "value": 105,
        "name": "UPITRANSFER",
        'usePaymentInfo': true
    };
    PaymentType.KOMOWALLET = {
        "value": 106,
        "name": "KOMOWALLET"
    };
    PaymentType.SHOPEEPAYWALLET = {
        "value": 107,
        "name": "SHOPEEPAYWALLET"
    };
    PaymentType.SHOPEEPAYTRANSFER = {
        "value": 108,
        "name": "SHOPEEPAYTRANSFER"
    };
    PaymentType.USDT_TRC20_TRANSFER = {
        "value": 109,
        "name": "USDT_TRC20_TRANSFER",
        "imageName": "trc20.png"
    };
    PaymentType.USDT_ERC20_TRANSFER = {
        "value": 110,
        "name": "USDT_ERC20_TRANSFER",
        "imageName": "erc20.png"
    };
    PaymentType.USDT_OMNI_TRANSFER = {
        "value": 111,
        "name": "USDT_OMNI_TRANSFER",
        "imageName": "omni.png"
    };
    PaymentType.ZALOWALLET = {
        "value": 112,
        "name": "ZALOWALLET"
    };
    PaymentType.YGPAY = {
        "value": 113,
        "name": "YGPay"
    };
    PaymentType.PERFECT_MONEY = {
        "value": 114,
        "name": "PERFECT_MONEY"
    };
    PaymentType.ALFA = {
        "value": 115,
        "name": "ALFA"
    };
    PaymentType.NIFT = {
        "value": 116,
        "name": "NIFT"
    };
    PaymentType.ONE_LINK_EBANKING = {
        "value": 117,
        "name": "ONE_LINK_EBANKING"
    };
    PaymentType.HBLKONNECT = {
        "value": 118,
        "name": "HBLKONNECT"
    };
    PaymentType.CONVENIENCESTORETRANSFER = {
        "value": 119,
        "name": "CONVENIENCESTORETRANSFER"
    };
    PaymentType.OMNIPAYWALLET = {
        "value": 120,
        "name": "OMNIPAYWALLET"
    };
    PaymentType.OMNIPAYTRANSFER = {
        "value": 121,
        "name": "OMNIPAYTRANSFER"
    };
    PaymentType.TAYOCASHWALLET = {
        "value": 122,
        "name": "TAYOCASHWALLET"
    };
    PaymentType.TAYOCASHTRANSFER = {
        "value": 123,
        "name": "TAYOCASHTRANSFER"
    };
    PaymentType.STARPAYWALLET = {
        "value": 124,
        "name": "STARPAYWALLET"
    };
    PaymentType.STARPAYTRANSFER = {
        "value": 125,
        "name": "STARPAYTRANSFER"
    };
    PaymentType.PRAXIS = {
        "value": 126,
        "name": "PRAXIS"
    };
    PaymentType.AMAZONPAYWALLET = {
        "value": 127,
        "name": "AMAZONPAYWALLET"
    };
    PaymentType.OLAMONEYWALLET = {
        "value": 128,
        "name": "OLAMONEYWALLET"
    };
    PaymentType.ITZCASHWALLET = {
        "value": 129,
        "name": "ITZCASHWALLET"
    };
    PaymentType.JIOMONEYWALLET = {
        "value": 130,
        "name": "JIOMONEYWALLET"
    };
    PaymentType.MPESAWALLET = {
        "value": 131,
        "name": "MPESAWALLET"
    };
    PaymentType.OXYGENWALLET = {
        "value": 132,
        "name": "OXYGENWALLET"
    };
    PaymentType.SBIBUDDY = {
        "value": 133,
        "name": "SBIBUDDY"
    };
    PaymentType.ZIPCASH = {
        "value": 134,
        "name": "ZIPCASH"
    };
    PaymentType.PAYNOWQRCODE = {
        "value": 135,
        "name": "PAYNOWQRCODE"
    };
    PaymentType.COINSPH = {
        "value": 136,
        "name": "COINSPH"
    };
    PaymentType.ASENSO = {
        "value": 137,
        "name": "ASENSO"
    };
    PaymentType.BAYAD = {
        "value": 138,
        "name": "BAYAD"
    };
    PaymentType.JUANCASH = {
        "value": 139,
        "name": "JUANCASH"
    };
    PaymentType.DCPay = {
        "value": 140,
        "name": "DCPay"
    };
    PaymentType.VIRTUALBANK = {
        "value": 141,
        "name": "VIRTUALBANK"
    };
    PaymentType.PIXBANK = {
        "value": 142,
        "name": "PIXBANK"
    };
    PaymentType.NAYAPAY = {
        "value": 143,
        "name": "NAYAPAY"
    };
    PaymentType.QRPAYMENT = {
        "value": 144,
        "name": "QRPAYMENT"
    };
    PaymentType.NAYAPAYTRANSFER = {
        "value": 145,
        "name": "NAYAPAYTRANSFER"
    };
    PaymentType.PIXBANKTRANSFER = {
        "value": 146,
        "name": "PIXBANKTRANSFER"
    };
    PaymentType.VISAMC = {
        "value": 147,
        "name": "VISAMC"
    };
    PaymentType.VISAMCTRANSFER = {
        "value": 148,
        "name": "VISAMCTRANSFER"
    };
    PaymentType.BILLEASE = {
        "value": 149,
        "name": "BILLEASE"
    };
    PaymentType.BILLEASETRANSFER = {
        "value": 150,
        "name": "BILLEASETRANSFER"
    };
    PaymentType.PALAWANPAWNSHOP = {
        "value": 151,
        "name": "PALAWANPAWNSHOP"
    };
    PaymentType.PALAWANPAWNSHOPTRANSFER = {
        "value": 152,
        "name": "PALAWANPAWNSHOPTRANSFER"
    };
    PaymentType.BTC = {
        "value": 153,
        "name": "BTC",
        "imageName": "btc.png"
    };
    PaymentType.ETH = {
        "value": 154,
        "name": "ETH",
        "imageName": "eth.png"
    };
    PaymentType.BTC_TRANSFER = {
        "value": 155,
        "name": "BTC_TRANSFER",
        "imageName": "btc.png"
    };
    PaymentType.ETH_TRANSFER = {
        "value": 156,
        "name": "ETH_TRANSFER",
        "imageName": "eth.png"
    };
    PaymentType.FIRSTPAY_TRANSFER = {
        "value": 157,
        "name": "FIRSTPAY_TRANSFER",
    };
    PaymentType.DUITNOWQR = {
        "value": 158,
        "name": "DUITNOWQR",
    };
    PaymentType.DUITNOWQRTRANSFER = {
        "value": 159,
        "name": "DUITNOWQRTRANSFER",
    };
    PaymentType.DUITNOWEWALLETQR = {
        "value": 160,
        "name": "DUITNOWEWALLETQR",
    };
    PaymentType.DUITNOWEWALLETQRTRANSFER = {
        "value": 161,
        "name": "DUITNOWEWALLETQRTRANSFER",
    };
    PaymentType.TOUCHNGOWALLET = {
        "value": 162,
        "name": "TOUCHNGOWALLET",
    };
    PaymentType.LAZADAWALLET = {
        "value": 163,
        "name": "LAZADAWALLET",
    };
    PaymentType.IREMIT = {
        "value": 164,
        "name": "IREMIT",
    };
    PaymentType.TRAXIONPAY = {
        "value": 165,
        "name": "TRAXIONPAY",
    };
    PaymentType.COINSPHTRANSFER = {
        "value": 166,
        "name": "COINSPHTRANSFER",
    };
    PaymentType.JAZZCASHTILLID = {
        "value": 167,
        "name": "JAZZCASHTILLID",
    };
    PaymentType.JAZZCASHTILLIDTRANSFER = {
        "value": 168,
        "name": "JAZZCASHTILLIDTRANSFER",
    };
    PaymentType.MONGOPAY = {
        "value": 169,
        "name": "MONGOPAY",
    };
    PaymentType.MONGOPAYTRANSFER = {
        "value": 170,
        "name": "MONGOPAYTRANSFER",
    };
    PaymentType.ECPAY = {
        "value": 171,
        "name": "ECPAY",
    };
    PaymentType.ECPAYTRANSFER = {
        "value": 172,
        "name": "ECPAYTRANSFER",
    };
    PaymentType.SCRATCHCARD = {
        "value": 173,
        "name": "CARDPCSCRATCHCARD",
    };
    PaymentType.SCRATCHCARDTRANSFER = {
        "value": 174,
        "name": "SCRATCHCARDTRANSFER",
    };
    PaymentType.GCASHQR = {
        "value": 175,
        "name": "GCASHQR",
    };
    PaymentType.PAYMAYAQR = {
        "value": 176,
        "name": "PAYMAYAQR",
    };
    PaymentType.UPIQR = {
        "value": 177,
        "name": "UPIQR",
    };
    PaymentType.ONLINEBANKINGQR = {
        "value": 178,
        "name": "ONLINEBANKINGQR",
    };
    PaymentType.CARDANO = {
        "value": 179,
        "name": "CARDANO",
    };
    PaymentType.DOGECOIN = {
        "value": 180,
        "name": "DOGECOIN",
    };
    PaymentType.LITECOIN = {
        "value": 181,
        "name": "LITECOIN",
    };
    PaymentType.XRP = {
        "value": 182,
        "name": "XRP",
    };
    PaymentType.TRON = {
        "value": 183,
        "name": "TRON",
    };
    PaymentType.CARDANO_TRANSFER = {
        "value": 184,
        "name": "CARDANOTRANSFER",
    };
    PaymentType.DOGECOIN_TRANSFER = {
        "value": 185,
        "name": "DOGECOINTRANSFER",
    };
    PaymentType.LITECOIN_TRANSFER = {
        "value": 186,
        "name": "LITECOINTRANSFER",
    };
    PaymentType.XRP_TRANSFER = {
        "value": 187,
        "name": "XRPTRANSFER",
    };
    PaymentType.TRON_TRANSFER = {
        "value": 188,
        "name": "TRONTRANSFER",
    };
    PaymentType.BOOST = {
        "value": 189,
        "name": "BOOST",
    };
    PaymentType.BIGPAY = {
        "value": 190,
        "name": "BIGPAY",
    };
    PaymentType.BIGPAYTRANSFER = {
        "value": 191,
        "name": "BIGPAYTRANSFER",
    };
    PaymentType.BANANAPAY = {
        "value": 192,
        "name": "BANANAPAY",
    };
    PaymentType.PDAX = {
        "value": 193,
        "name": "PDAX",
    };
    PaymentType.EMANGO = {
        "value": 194,
        "name": "EMANGO",
    };

    (function () {
        for (atr in PaymentType) {
            var obj = PaymentType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PaymentType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PaymentType.values = function () {
        return objects;
    };
})();

/*
 *
 */
if (typeof (CompanyBankPaymentType) == 'undefined') {
    CompanyBankPaymentType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    CompanyBankPaymentType.REAL_BANK = {
        "value": 1,
        "name": "Real Bank"
    };
    CompanyBankPaymentType.WE_CHAT = {
        "value": 2,
        "name": "WeChat"
    };
    CompanyBankPaymentType.ALI_PAY = {
        "value": 3,
        "name": "AliPay"
    };
    CompanyBankPaymentType.QQ = {
        "value": 4,
        "name": "QQ"
    };
    CompanyBankPaymentType.MOMO_PAY = {
        "value": 5,
        "name": "MOMOPay"
    };
    CompanyBankPaymentType.BKASH = {
        "value": 6,
        "name": "bKash"
    };
    CompanyBankPaymentType.ROCKET = {
        "value": 7,
        "name": "Rocket"
    };
    CompanyBankPaymentType.NAGAD = {
        "value": 8,
        "name": "Nagad"
    };
    CompanyBankPaymentType.PHONEPE = {
        "value": 9,
        "name": "PhonePe"
    };
    CompanyBankPaymentType.EASYPAISA = {
        "value": 10,
        "name": "Easypaisa"
    };
    CompanyBankPaymentType.JAZZCASH = {
        "value": 11,
        "name": "Jazzcash"
    };
    CompanyBankPaymentType.ZALO = {
        "value": 12,
        "name": "Zalo"
    };
    CompanyBankPaymentType.CASHMAAL = {
        "value": 13,
        "name": "Cashmaal"
    };
    CompanyBankPaymentType.GCASH = {
        "value": 14,
        "name": "GCash"
    };
    CompanyBankPaymentType.GRABPAY = {
        "value": 15,
        "name": "GrabPay"
    };
    CompanyBankPaymentType.PAYMAYA = {
        "value": 16,
        "name": "PyaMaya"
    };
    CompanyBankPaymentType.UPAY = {
        "value": 17,
        "name": "UPAY"
    };

    CompanyBankPaymentType.BOOSTWALLET = {
        "value": 18,
        "name": "BOOSTWALLET"
    };

    CompanyBankPaymentType.TOUCHNGO = {
        "value": 19,
        "name": "TOUCHNGO"
    };

    CompanyBankPaymentType.GPAY = {
        "value": 20,
        "name": "GPAY"
    };

    CompanyBankPaymentType.PAYTM = {
        "value": 21,
        "name": "PAYTM"
    };

    CompanyBankPaymentType.SURECASH = {
        "value": 22,
        "name": "SURECASH"
    };

    CompanyBankPaymentType.TRUSTAXIATAPAY = {
        "value": 23,
        "name": "TRUSTAXIATAPAY"
    };

    CompanyBankPaymentType.OKWALLET = {
        "value": 24,
        "name": "OKWALLET"
    };

    CompanyBankPaymentType.UPAISA = {
        "value": 25,
        "name": "UPAISA"
    };

    CompanyBankPaymentType.UPI = {
        "value": 26,
        "name": "UPI"
    };

    CompanyBankPaymentType.KOMO = {
        "value": 27,
        "name": "KOMO"
    };

    CompanyBankPaymentType.USDT_TRC20 = {
        "value": 28,
        "name": "USDT_TRC20"
    };

    CompanyBankPaymentType.USDT_ERC20 = {
        "value": 29,
        "name": "USDT_ERC20"
    };

    CompanyBankPaymentType.USDT_OMNI = {
        "value": 30,
        "name": "USDT_OMNI"
    };
    CompanyBankPaymentType.CONVENIENCESTORE = {
        "value": 31,
        "name": "7-11"
    };
    CompanyBankPaymentType.SHOPEEPAY = {
        "value": 32,
        "name": "SHOPEEPAY"
    };
    CompanyBankPaymentType.OMNIPAY = {
        "value": 33,
        "name": "OMNIPAY"
    };
    CompanyBankPaymentType.TAYOCASH = {
        "value": 34,
        "name": "TAYOCASH"
    };
    CompanyBankPaymentType.STARPAY = {
        "value": 35,
        "name": "STARPAY"
    };
    CompanyBankPaymentType.PAYNOWQRCODE = {
        "value": 36,
        "name": "PAYNOWQRCODE"
    };
    CompanyBankPaymentType.VIRTUALBANK = {
        "value": 37,
        "name": "VIRTUALBANK"
    };
    CompanyBankPaymentType.NAYAPAY = {
        "value": 38,
        "name": "NAYAPAY"
    };
    CompanyBankPaymentType.PIXBANK = {
        "value": 39,
        "name": "PIXBANK"
    };
    CompanyBankPaymentType.VISAMC = {
        "value": 40,
        "name": "VISAMC"
    };
    CompanyBankPaymentType.BILLEASE = {
        "value": 41,
        "name": "BILLEASE"
    };
    CompanyBankPaymentType.PALAWANPAWNSHOP = {
        "value": 42,
        "name": "PALAWANPAWNSHOP"
    };
    CompanyBankPaymentType.FIRSTPAY = {
        "value": 45,
        "name": "FIRSTPAY"
    };
    CompanyBankPaymentType.COINSPH = {
        "value": 46,
        "name": "COINSPH"
    };
    CompanyBankPaymentType.JAZZCASHTILLID = {
        "value": 47,
        "name": "JAZZCASHTILLID"
    };
    CompanyBankPaymentType.ECPAY = {
        "value": 48,
        "name": "ECPAY"
    };


    (function () {
        for (atr in CompanyBankPaymentType) {
            var obj = CompanyBankPaymentType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    CompanyBankPaymentType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CompanyBankPaymentType.values = function () {
        return objects;
    };
})();

/*
 *
 */
if (typeof (PromotionItemType) == 'undefined') {
    PromotionItemType = {};
}

(function () {

    var getValue = function () {
        return this.value;
    };

    var objects = [];

    PromotionItemType.PROMOTION_MESSAGE = {
        "value": -1
    };
    PromotionItemType.NORMAL_DEPOSIT = {
        "value": 0
    };
    PromotionItemType.FIRST_DEPOSIT = {
        "value": 1
    };
//	PromotionItemType.RELOAD_DEPOSIT = {
//		"value" : 2
//	};

    (function () {
        for (atr in PromotionItemType) {
            var obj = PromotionItemType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PromotionItemType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PromotionItemType.values = function () {
        return objects;
    };

})();

if (typeof (SemiAutoBonusStatusType) == 'undefined') {
    SemiAutoBonusStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    SemiAutoBonusStatusType.PENDING = {
        "value": 0,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.semiAutoBonusStatusType.PENDING";
        },
    };
    SemiAutoBonusStatusType.ISSUED = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Issued";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.semiAutoBonusStatusType.ISSUED";
        },
    };
    SemiAutoBonusStatusType.CANCEL = {
        "value": 2,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Cancelled";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.semiAutoBonusStatusType.CANCEL";
        },
    };

    (function () {
        for (atr in SemiAutoBonusStatusType) {
            var obj = SemiAutoBonusStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    SemiAutoBonusStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    SemiAutoBonusStatusType.values = function () {
        return objects;
    };
})();

if (typeof (PromotionVerificationStatusType) == 'undefined') {
    PromotionVerificationStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PromotionVerificationStatusType.PENDING = {
        "value": 0,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.NEW";
        },
    };
    PromotionVerificationStatusType.APPROVED = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Approved";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.CONFIRMED";
        },
    };
    PromotionVerificationStatusType.REJECTED = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Rejected";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.REJECTED";
        },
    };
    PromotionVerificationStatusType.REJECTED_FOREVER = {
        "value": -2,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Rejected Forever";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.CLOSE";
        },
    };

    (function () {
        for (atr in PromotionVerificationStatusType) {
            var obj = PromotionVerificationStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PromotionVerificationStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PromotionVerificationStatusType.values = function () {
        return objects;
    };
})();


if (typeof (ManualForceServeStatusType) == 'undefined') {
    ManualForceServeStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ManualForceServeStatusType.PENDING = {
        "value": 0,
        "getClassName": function () {
            return "label-primary";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.NEW";
        },
    };
    ManualForceServeStatusType.ON_HOLD = {
        "value": 1,
        "getClassName": function () {
            return "label-info";
        },
        "getName": function () {
            return "On Hold";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.ON_HOLD";
        },
    };
    ManualForceServeStatusType.CONFIRMED = {
        "value": 2,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Approved";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.CONFIRMED";
        },
    };
    ManualForceServeStatusType.REJECTED = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Rejected";
        },
        "getDisplayName": function () {
            return "global.text.moneyTransactionStatusType.REJECTED";
        },
    };

    (function () {
        for (atr in ManualForceServeStatusType) {
            var obj = ManualForceServeStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ManualForceServeStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ManualForceServeStatusType.values = function () {
        return objects;
    };
})();

if (typeof (AffiliateFinanceStatusType) == 'undefined') {
    AffiliateFinanceStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AffiliateFinanceStatusType.PENDING = {
        "value": 0,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.affiliateFinanceStatusType.PENDING";
        },
    };
    AffiliateFinanceStatusType.VERIFIED = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Issued";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.affiliateFinanceStatusType.VERIFIED";
        },
    };
    AffiliateFinanceStatusType.CANCEL = {
        "value": 2,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Cancelled";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.affiliateFinanceStatusType.CANCEL";
        },
    };

    (function () {
        for (atr in AffiliateFinanceStatusType) {
            var obj = AffiliateFinanceStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AffiliateFinanceStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AffiliateFinanceStatusType.values = function () {
        return objects;
    };
})();

/*
 *
 */
if (typeof (PGAccountActionType) == 'undefined') {
    PGAccountActionType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    PGAccountActionType.CREATE = {
        "value": 1
    };
    PGAccountActionType.UPDATE = {
        "value": 2
    };
    PGAccountActionType.TEST = {
        "value": 3
    };

    (function () {
        for (atr in PGAccountActionType) {
            var obj = PGAccountActionType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PGAccountActionType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    PGAccountActionType.values = function () {
        return objects;
    };
})();

/*
*
*/
if (typeof (CustomerServiceType) == 'undefined') {
    CustomerServiceType = {};
}

(function () {

    var objects = [];

    CustomerServiceType.CSLINK = {
        "value": 1,
        "displayName": "CSlink",
        "headerCsClass": 'icon-cs',
        "memberMenuIconClass": "talk",
    };
    CustomerServiceType.EMAIL = {
        "value": 2,
        "displayName": "email",
        "headerCsClass": 'icon-mail',
        "memberMenuIconClass": "Email",
    };
    CustomerServiceType.QQ = {
        "value": 3,
        "displayName": "QQ",
        "headerCsClass": 'icon-QQ',
        "memberMenuIconClass": "qq",
    };
    CustomerServiceType.WECHAT = {
        "value": 4,
        "displayName": "WeChat",
        "headerCsClass": 'icon-wechat',
        "memberMenuIconClass": "wechat",
    };
    CustomerServiceType.SKYPE = {
        "value": 5,
        "displayName": "Skype",
        "headerCsClass": 'icon-skype',
        "memberMenuIconClass": "skype",
    };
    CustomerServiceType.ZALO = {
        "value": 6,
        "displayName": "Zalo",
        "headerCsClass": 'icon-zalo',
        "memberMenuIconClass": "zalo",
    };
    CustomerServiceType.TELEGRAM = {
        "value": 7,
        "displayName": "Telegram",
        "headerCsClass": 'icon-telegram',
        "memberMenuIconClass": "telegram",
    };
    CustomerServiceType.WHATSAPP = {
        "value": 8,
        "displayName": "WhatsApp",
        "headerCsClass": 'icon-whatsapp',
        "memberMenuIconClass": "whatsapp",
    };
    CustomerServiceType.KAKAOTALK = {
        "value": 9,
        "displayName": "KakaoTalk",
        "headerCsClass": 'icon-kakao',
        "memberMenuIconClass": "kakao-talk",
    };
    CustomerServiceType.LINE = {
        "value": 10,
        "displayName": "LINE",
        "headerCsClass": 'icon-line',
        "memberMenuIconClass": "line",
    };
    CustomerServiceType.BBM = {
        "value": 11,
        "displayName": "BBM",
        "headerCsClass": 'icon-bbm',
        "memberMenuIconClass": "bbm",
    };
    CustomerServiceType.FACEBOOK = {
        "value": 12,
        "displayName": "Facebook Messenger",
        "headerCsClass": 'icon-fb',
        "memberMenuIconClass": "facebook-messenger",
    };
    CustomerServiceType.IMO = {
        "value": 13,
        "displayName": "imo",
        "headerCsClass": 'icon-imo',
        "memberMenuIconClass": "imo",
    };
    CustomerServiceType.Phone = {
        "value": 14,
        "displayName": "Phone",
        "headerCsClass": 'icon-phone',
        "memberMenuIconClass": "phone",
    };
    CustomerServiceType.Viber = {
        "value": 15,
        "displayName": "Viber",
        "headerCsClass": 'icon-viber',
        "memberMenuIconClass": "viber",
    };

    (function () {
        for (atr in CustomerServiceType) {
            var obj = CustomerServiceType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
        }
    }());

    CustomerServiceType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    CustomerServiceType.values = function () {
        return objects;
    };
}());

/*
*
*/
if (typeof (LandingPageType) == 'undefined') {
    LandingPageType = {};
}

(function () {

    var objects = [];

    LandingPageType.ALL = {
        "value": 0,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.ALL";
        },
    };

    LandingPageType.MAIN = {
        "value": 1,
        "path": "/index.jsp",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.MAIN";
        },
    };

    LandingPageType.REGISTER = {
        "value": 2,
        "path": "/register.jsp",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.REGISTER";
        },
    };

    LandingPageType.PROMOTION = {
        "value": 3,
        "path": "/promotions.jsp",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.PROMOTION";
        },
    };

    LandingPageType.SPORTS = {
        "value": 4,
        "path": "/sport/",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.SPORTS";
        },
    };

    LandingPageType.CASINO = {
        "value": 5,
        "path": "/casino/",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.CASINO";
        },
    };

    LandingPageType.CARD = {
        "value": 6,
        "path": "/card/",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.CARD";
        },
    };

    LandingPageType.FISHING = {
        "value": 7,
        "path": "/fishing/",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.FISHING";
        },
    };

    LandingPageType.SLOT = {
        "value": 8,
        "path": "/slot/",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.SLOT";
        },
    };

    LandingPageType.VIP = {
        "value": 9,
        "path": "/vip/",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.VIP";
        },
    };

    LandingPageType.ABOUT = {
        "value": 10,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.ABOUT";
        },
    };

    LandingPageType.PAYMENT = {
        "value": 11,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.PAYMENT";
        },
    };

    LandingPageType.PRIVACY = {
        "value": 12,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.PRIVACY";
        },
    };

    LandingPageType.CONDITIONS = {
        "value": 13,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.CONDITIONS";
        },
    };

    LandingPageType.DUTY = {
        "value": 14,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.DUTY";
        },
    };

    LandingPageType.AGE_18_ABOVE = {
        "value": 15,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.AGE_18_ABOVE";
        },
    };

    LandingPageType.FAQ = {
        "value": 16,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.FAQ";
        },
    };

    LandingPageType.RULE = {
        "value": 17,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.RULE";
        },
    };

    LandingPageType.CONTACT = {
        "value": 18,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.CONTACT";
        },
    };

    LandingPageType.SAFE = {
        "value": 19,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.SAFE";
        },
    };

    LandingPageType.NOT_FOUND = {
        "value": 19,
        "path": "",
        "getDisplayName": function () {
            return "form.text.affiliate.landingPageType.NOT_FOUND";
        },
    };


    (function () {
        for (atr in LandingPageType) {
            var obj = LandingPageType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
        }
    }());

    LandingPageType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    LandingPageType.values = function () {
        return objects;
    };

}());


if (typeof (SearchEngineOptimizationDefaultAttribute) == 'undefined') {
    SearchEngineOptimizationDefaultAttribute = {};
}

(function () {

    var objects = [];

    SearchEngineOptimizationDefaultAttribute.TITLE = {
        "value": 1,
        "getName": "title"
    };

    SearchEngineOptimizationDefaultAttribute.KEYWORDS = {
        "value": 2,
        "getName": "keywords"
    };

    SearchEngineOptimizationDefaultAttribute.DESCRIPTION = {
        "value": 3,
        "getName": "description"
    };


    (function () {
        for (atr in SearchEngineOptimizationDefaultAttribute) {
            var obj = SearchEngineOptimizationDefaultAttribute[atr];
            objects[objects.length] = obj;
            obj.name = atr;
        }
    }());

    SearchEngineOptimizationDefaultAttribute.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    SearchEngineOptimizationDefaultAttribute.values = function () {
        return objects;
    };

}());


if (typeof (WithdrawalProvinceCityData) == 'undefined') {
    WithdrawalProvinceCityData = {};
}

(function () {

    var provinceList = [
        {"id": "110000", "name": "北京市"},
        {"id": "120000", "name": "天津市"},
        {"id": "130000", "name": "河北省"},
        {"id": "140000", "name": "山西省"},
        {"id": "150000", "name": "内蒙古自治区"},
        {"id": "210000", "name": "辽宁省"},
        {"id": "220000", "name": "吉林省"},
        {"id": "230000", "name": "黑龙江省"},
        {"id": "310000", "name": "上海市"},
        {"id": "320000", "name": "江苏省"},
        {"id": "330000", "name": "浙江省"},
        {"id": "340000", "name": "安徽省"},
        {"id": "350000", "name": "福建省"},
        {"id": "360000", "name": "江西省"},
        {"id": "370000", "name": "山东省"},
        {"id": "410000", "name": "河南省"},
        {"id": "420000", "name": "湖北省"},
        {"id": "430000", "name": "湖南省"},
        {"id": "440000", "name": "广东省"},
        {"id": "450000", "name": "广西壮族自治区"},
        {"id": "460000", "name": "海南省"},
        {"id": "500000", "name": "重庆市"},
        {"id": "510000", "name": "四川省"},
        {"id": "520000", "name": "贵州省"},
        {"id": "530000", "name": "云南省"},
        {"id": "540000", "name": "西藏自治区"},
        {"id": "610000", "name": "陕西省"},
        {"id": "620000", "name": "甘肃省"},
        {"id": "630000", "name": "青海省"},
        {"id": "640000", "name": "宁夏回族自治区"},
        {"id": "650000", "name": "新疆维吾尔自治区"}
    ];

    var cityList = {
        "110000": [{"id": "110100", "name": "北京市"}],
        "120000": [{"id": "120100", "name": "天津市"}],
        "130000": [{"id": "130100", "name": "石家庄市"}, {"id": "130200", "name": "唐山市"}, {
            "id": "130300",
            "name": "秦皇岛市"
        }, {"id": "130400", "name": "邯郸市"}, {"id": "130500", "name": "邢台市"}, {
            "id": "130600",
            "name": "保定市"
        }, {"id": "130700", "name": "张家口市"}, {"id": "130800", "name": "承德市"}, {
            "id": "130900",
            "name": "沧州市"
        }, {"id": "131000", "name": "廊坊市"}, {"id": "131100", "name": "衡水市"}],
        "140000": [{"id": "140100", "name": "太原市"}, {"id": "140200", "name": "大同市"}, {
            "id": "140300",
            "name": "阳泉市"
        }, {"id": "140400", "name": "长治市"}, {"id": "140500", "name": "晋城市"}, {
            "id": "140600",
            "name": "朔州市"
        }, {"id": "140700", "name": "晋中市"}, {"id": "140800", "name": "运城市"}, {
            "id": "140900",
            "name": "忻州市"
        }, {"id": "141000", "name": "临汾市"}, {"id": "141100", "name": "吕梁市"}],
        "150000": [{"id": "150100", "name": "呼和浩特市"}, {"id": "150200", "name": "包头市"}, {
            "id": "150300",
            "name": "乌海市"
        }, {"id": "150400", "name": "赤峰市"}, {"id": "150500", "name": "通辽市"}, {
            "id": "150600",
            "name": "鄂尔多斯市"
        }, {"id": "150700", "name": "呼伦贝尔市"}, {"id": "150800", "name": "巴彦淖尔市"}, {
            "id": "150900",
            "name": "乌兰察布市"
        }, {"id": "152200", "name": "兴安盟"}, {"id": "152500", "name": "锡林郭勒盟"}, {
            "id": "152900",
            "name": "阿拉善盟"
        }],
        "210000": [{"id": "210100", "name": "沈阳市"}, {"id": "210200", "name": "大连市"}, {
            "id": "210300",
            "name": "鞍山市"
        }, {"id": "210400", "name": "抚顺市"}, {"id": "210500", "name": "本溪市"}, {
            "id": "210600",
            "name": "丹东市"
        }, {"id": "210700", "name": "锦州市"}, {"id": "210800", "name": "营口市"}, {
            "id": "210900",
            "name": "阜新市"
        }, {"id": "211000", "name": "辽阳市"}, {"id": "211100", "name": "盘锦市"}, {
            "id": "211200",
            "name": "铁岭市"
        }, {"id": "211300", "name": "朝阳市"}, {"id": "211400", "name": "葫芦岛市"}],
        "220000": [{"id": "220100", "name": "长春市"}, {"id": "220200", "name": "吉林市"}, {
            "id": "220300",
            "name": "四平市"
        }, {"id": "220400", "name": "辽源市"}, {"id": "220500", "name": "通化市"}, {
            "id": "220600",
            "name": "白山市"
        }, {"id": "220700", "name": "松原市"}, {"id": "220800", "name": "白城市"}, {
            "id": "222400",
            "name": "延边朝鲜族自治州"
        }],
        "230000": [{"id": "230100", "name": "哈尔滨市"}, {"id": "230200", "name": "齐齐哈尔市"}, {
            "id": "230300",
            "name": "鸡西市"
        }, {"id": "230400", "name": "鹤岗市"}, {"id": "230500", "name": "双鸭山市"}, {
            "id": "230600",
            "name": "大庆市"
        }, {"id": "230700", "name": "伊春市"}, {"id": "230800", "name": "佳木斯市"}, {
            "id": "230900",
            "name": "七台河市"
        }, {"id": "231000", "name": "牡丹江市"}, {"id": "231100", "name": "黑河市"}, {
            "id": "231200",
            "name": "绥化市"
        }, {"id": "232700", "name": "大兴安岭地区"}],
        "310000": [{"id": "310100", "name": "上海市"}],
        "320000": [{"id": "320100", "name": "南京市"}, {"id": "320200", "name": "无锡市"}, {
            "id": "320300",
            "name": "徐州市"
        }, {"id": "320400", "name": "常州市"}, {"id": "320500", "name": "苏州市"}, {
            "id": "320600",
            "name": "南通市"
        }, {"id": "320700", "name": "连云港市"}, {"id": "320800", "name": "淮安市"}, {
            "id": "320900",
            "name": "盐城市"
        }, {"id": "321000", "name": "扬州市"}, {"id": "321100", "name": "镇江市"}, {
            "id": "321200",
            "name": "泰州市"
        }, {"id": "321300", "name": "宿迁市"}],
        "330000": [{"id": "330100", "name": "杭州市"}, {"id": "330200", "name": "宁波市"}, {
            "id": "330300",
            "name": "温州市"
        }, {"id": "330400", "name": "嘉兴市"}, {"id": "330500", "name": "湖州市"}, {
            "id": "330600",
            "name": "绍兴市"
        }, {"id": "330700", "name": "金华市"}, {"id": "330800", "name": "衢州市"}, {
            "id": "330900",
            "name": "舟山市"
        }, {"id": "331000", "name": "台州市"}, {"id": "331100", "name": "丽水市"}],
        "340000": [{"id": "340100", "name": "合肥市"}, {"id": "340200", "name": "芜湖市"}, {
            "id": "340300",
            "name": "蚌埠市"
        }, {"id": "340400", "name": "淮南市"}, {"id": "340500", "name": "马鞍山市"}, {
            "id": "340600",
            "name": "淮北市"
        }, {"id": "340700", "name": "铜陵市"}, {"id": "340800", "name": "安庆市"}, {
            "id": "341000",
            "name": "黄山市"
        }, {"id": "341100", "name": "滁州市"}, {"id": "341200", "name": "阜阳市"}, {
            "id": "341300",
            "name": "宿州市"
        }, {"id": "341500", "name": "六安市"}, {"id": "341600", "name": "亳州市"}, {
            "id": "341700",
            "name": "池州市"
        }, {"id": "341800", "name": "宣城市"}],
        "350000": [{"id": "350100", "name": "福州市"}, {"id": "350200", "name": "厦门市"}, {
            "id": "350300",
            "name": "莆田市"
        }, {"id": "350400", "name": "三明市"}, {"id": "350500", "name": "泉州市"}, {
            "id": "350600",
            "name": "漳州市"
        }, {"id": "350700", "name": "南平市"}, {"id": "350800", "name": "龙岩市"}, {"id": "350900", "name": "宁德市"}],
        "360000": [{"id": "360100", "name": "南昌市"}, {"id": "360200", "name": "景德镇市"}, {
            "id": "360300",
            "name": "萍乡市"
        }, {"id": "360400", "name": "九江市"}, {"id": "360500", "name": "新余市"}, {
            "id": "360600",
            "name": "鹰潭市"
        }, {"id": "360700", "name": "赣州市"}, {"id": "360800", "name": "吉安市"}, {
            "id": "360900",
            "name": "宜春市"
        }, {"id": "361000", "name": "抚州市"}, {"id": "361100", "name": "上饶市"}],
        "370000": [{"id": "370100", "name": "济南市"}, {"id": "370200", "name": "青岛市"}, {
            "id": "370300",
            "name": "淄博市"
        }, {"id": "370400", "name": "枣庄市"}, {"id": "370500", "name": "东营市"}, {
            "id": "370600",
            "name": "烟台市"
        }, {"id": "370700", "name": "潍坊市"}, {"id": "370800", "name": "济宁市"}, {
            "id": "370900",
            "name": "泰安市"
        }, {"id": "371000", "name": "威海市"}, {"id": "371100", "name": "日照市"}, {
            "id": "371200",
            "name": "莱芜市"
        }, {"id": "371300", "name": "临沂市"}, {"id": "371400", "name": "德州市"}, {
            "id": "371500",
            "name": "聊城市"
        }, {"id": "371600", "name": "滨州市"}, {"id": "371700", "name": "菏泽市"}],
        "410000": [{"id": "410100", "name": "郑州市"}, {"id": "410200", "name": "开封市"}, {
            "id": "410300",
            "name": "洛阳市"
        }, {"id": "410400", "name": "平顶山市"}, {"id": "410500", "name": "安阳市"}, {
            "id": "410600",
            "name": "鹤壁市"
        }, {"id": "410700", "name": "新乡市"}, {"id": "410800", "name": "焦作市"}, {
            "id": "410900",
            "name": "濮阳市"
        }, {"id": "411000", "name": "许昌市"}, {"id": "411100", "name": "漯河市"}, {
            "id": "411200",
            "name": "三门峡市"
        }, {"id": "411300", "name": "南阳市"}, {"id": "411400", "name": "商丘市"}, {
            "id": "411500",
            "name": "信阳市"
        }, {"id": "411600", "name": "周口市"}, {"id": "411700", "name": "驻马店市"}],
        "420000": [{"id": "420100", "name": "武汉市"}, {"id": "420200", "name": "黄石市"}, {
            "id": "420300",
            "name": "十堰市"
        }, {"id": "420500", "name": "宜昌市"}, {"id": "420600", "name": "襄阳市"}, {
            "id": "420700",
            "name": "鄂州市"
        }, {"id": "420800", "name": "荆门市"}, {"id": "420900", "name": "孝感市"}, {
            "id": "421000",
            "name": "荆州市"
        }, {"id": "421100", "name": "黄冈市"}, {"id": "421200", "name": "咸宁市"}, {
            "id": "421300",
            "name": "随州市"
        }, {"id": "422800", "name": "恩施土家族苗族自治州"}],
        "430000": [{"id": "430100", "name": "长沙市"}, {"id": "430200", "name": "株洲市"}, {
            "id": "430300",
            "name": "湘潭市"
        }, {"id": "430400", "name": "衡阳市"}, {"id": "430500", "name": "邵阳市"}, {
            "id": "430600",
            "name": "岳阳市"
        }, {"id": "430700", "name": "常德市"}, {"id": "430800", "name": "张家界市"}, {
            "id": "430900",
            "name": "益阳市"
        }, {"id": "431000", "name": "郴州市"}, {"id": "431100", "name": "永州市"}, {
            "id": "431200",
            "name": "怀化市"
        }, {"id": "431300", "name": "娄底市"}, {"id": "433100", "name": "湘西土家族苗族自治州"}],
        "440000": [{"id": "440100", "name": "广州市"}, {"id": "440200", "name": "韶关市"}, {
            "id": "440300",
            "name": "深圳市"
        }, {"id": "440400", "name": "珠海市"}, {"id": "440500", "name": "汕头市"}, {
            "id": "440600",
            "name": "佛山市"
        }, {"id": "440700", "name": "江门市"}, {"id": "440800", "name": "湛江市"}, {
            "id": "440900",
            "name": "茂名市"
        }, {"id": "441200", "name": "肇庆市"}, {"id": "441300", "name": "惠州市"}, {
            "id": "441400",
            "name": "梅州市"
        }, {"id": "441500", "name": "汕尾市"}, {"id": "441600", "name": "河源市"}, {
            "id": "441700",
            "name": "阳江市"
        }, {"id": "441800", "name": "清远市"}, {"id": "441900", "name": "东莞市"}, {
            "id": "442000",
            "name": "中山市"
        }, {"id": "445100", "name": "潮州市"}, {"id": "445200", "name": "揭阳市"}, {"id": "445300", "name": "云浮市"}],
        "450000": [{"id": "450100", "name": "南宁市"}, {"id": "450200", "name": "柳州市"}, {
            "id": "450300",
            "name": "桂林市"
        }, {"id": "450400", "name": "梧州市"}, {"id": "450500", "name": "北海市"}, {
            "id": "450600",
            "name": "防城港市"
        }, {"id": "450700", "name": "钦州市"}, {"id": "450800", "name": "贵港市"}, {
            "id": "450900",
            "name": "玉林市"
        }, {"id": "451000", "name": "百色市"}, {"id": "451100", "name": "贺州市"}, {
            "id": "451200",
            "name": "河池市"
        }, {"id": "451300", "name": "来宾市"}, {"id": "451400", "name": "崇左市"}],
        "460000": [{"id": "460100", "name": "海口市"}, {"id": "460200", "name": "三亚市"}, {
            "id": "460300",
            "name": "三沙市"
        }, {"id": "460400", "name": "儋州市"}],
        "500000": [{"id": "500100", "name": "重庆市"}],
        "510000": [{"id": "510100", "name": "成都市"}, {"id": "510300", "name": "自贡市"}, {
            "id": "510400",
            "name": "攀枝花市"
        }, {"id": "510500", "name": "泸州市"}, {"id": "510600", "name": "德阳市"}, {
            "id": "510700",
            "name": "绵阳市"
        }, {"id": "510800", "name": "广元市"}, {"id": "510900", "name": "遂宁市"}, {
            "id": "511000",
            "name": "内江市"
        }, {"id": "511100", "name": "乐山市"}, {"id": "511300", "name": "南充市"}, {
            "id": "511400",
            "name": "眉山市"
        }, {"id": "511500", "name": "宜宾市"}, {"id": "511600", "name": "广安市"}, {
            "id": "511700",
            "name": "达州市"
        }, {"id": "511800", "name": "雅安市"}, {"id": "511900", "name": "巴中市"}, {
            "id": "512000",
            "name": "资阳市"
        }, {"id": "513200", "name": "阿坝藏族羌族自治州"}, {"id": "513300", "name": "甘孜藏族自治州"}, {
            "id": "513400",
            "name": "凉山彝族自治州"
        }],
        "520000": [{"id": "520100", "name": "贵阳市"}, {"id": "520200", "name": "六盘水市"}, {
            "id": "520300",
            "name": "遵义市"
        }, {"id": "520400", "name": "安顺市"}, {"id": "520500", "name": "毕节市"}, {
            "id": "520600",
            "name": "铜仁市"
        }, {"id": "522300", "name": "黔西南布依族苗族自治州"}, {"id": "522600", "name": "黔东南苗族侗族自治州"}, {
            "id": "522700",
            "name": "黔南布依族苗族自治州"
        }],
        "530000": [{"id": "530100", "name": "昆明市"}, {"id": "530300", "name": "曲靖市"}, {
            "id": "530400",
            "name": "玉溪市"
        }, {"id": "530500", "name": "保山市"}, {"id": "530600", "name": "昭通市"}, {
            "id": "530700",
            "name": "丽江市"
        }, {"id": "530800", "name": "普洱市"}, {"id": "530900", "name": "临沧市"}, {
            "id": "532300",
            "name": "楚雄彝族自治州"
        }, {"id": "532500", "name": "红河哈尼族彝族自治州"}, {"id": "532600", "name": "文山壮族苗族自治州"}, {
            "id": "532800",
            "name": "西双版纳傣族自治州"
        }, {"id": "532900", "name": "大理白族自治州"}, {"id": "533100", "name": "德宏傣族景颇族自治州"}, {
            "id": "533300",
            "name": "怒江傈僳族自治州"
        }, {"id": "533400", "name": "迪庆藏族自治州"}],
        "540000": [{"id": "540100", "name": "拉萨市"}, {"id": "540200", "name": "日喀则市"}, {
            "id": "540300",
            "name": "昌都市"
        }, {"id": "540400", "name": "林芝市"}, {"id": "540500", "name": "山南市"}, {
            "id": "542400",
            "name": "那曲地区"
        }, {"id": "542500", "name": "阿里地区"}],
        "610000": [{"id": "610100", "name": "西安市"}, {"id": "610200", "name": "铜川市"}, {
            "id": "610300",
            "name": "宝鸡市"
        }, {"id": "610400", "name": "咸阳市"}, {"id": "610500", "name": "渭南市"}, {
            "id": "610600",
            "name": "延安市"
        }, {"id": "610700", "name": "汉中市"}, {"id": "610800", "name": "榆林市"}, {
            "id": "610900",
            "name": "安康市"
        }, {"id": "611000", "name": "商洛市"}],
        "620000": [{"id": "620100", "name": "兰州市"}, {"id": "620200", "name": "嘉峪关市"}, {
            "id": "620300",
            "name": "金昌市"
        }, {"id": "620400", "name": "白银市"}, {"id": "620500", "name": "天水市"}, {
            "id": "620600",
            "name": "武威市"
        }, {"id": "620700", "name": "张掖市"}, {"id": "620800", "name": "平凉市"}, {
            "id": "620900",
            "name": "酒泉市"
        }, {"id": "621000", "name": "庆阳市"}, {"id": "621100", "name": "定西市"}, {
            "id": "621200",
            "name": "陇南市"
        }, {"id": "622900", "name": "临夏回族自治州"}, {"id": "623000", "name": "甘南藏族自治州"}],
        "630000": [{"id": "630100", "name": "西宁市"}, {"id": "630200", "name": "海东市"}, {
            "id": "632200",
            "name": "海北藏族自治州"
        }, {"id": "632300", "name": "黄南藏族自治州"}, {"id": "632500", "name": "海南藏族自治州"}, {
            "id": "632600",
            "name": "果洛藏族自治州"
        }, {"id": "632700", "name": "玉树藏族自治州"}, {"id": "632800", "name": "海西蒙古族藏族自治州"}],
        "640000": [{"id": "640100", "name": "银川市"}, {"id": "640200", "name": "石嘴山市"}, {
            "id": "640300",
            "name": "吴忠市"
        }, {"id": "640400", "name": "固原市"}, {"id": "640500", "name": "中卫市"}],
        "650000": [{"id": "650100", "name": "乌鲁木齐市"}, {"id": "650200", "name": "克拉玛依市"}, {
            "id": "650400",
            "name": "吐鲁番市"
        }, {"id": "650500", "name": "哈密市"}, {"id": "652300", "name": "昌吉回族自治州"}, {
            "id": "652700",
            "name": "博尔塔拉蒙古自治州"
        }, {"id": "652800", "name": "巴音郭楞蒙古自治州"}, {"id": "652900", "name": "阿克苏地区"}, {
            "id": "653000",
            "name": "克孜勒苏柯尔克孜自治州"
        }, {"id": "653100", "name": "喀什地区"}, {"id": "653200", "name": "和田地区"}, {
            "id": "654000",
            "name": "伊犁哈萨克自治州"
        }, {"id": "654200", "name": "塔城地区"}, {"id": "654300", "name": "阿勒泰地区"}, {
            "id": "659000",
            "name": "自治区直辖县级行政区划"
        }]
    };

    WithdrawalProvinceCityData.getAllProvince = function () {
        return provinceList;
    };

    WithdrawalProvinceCityData.getProvinceAllCity = function (provinceId) {
        return cityList[provinceId];
    };

}());


if (typeof (BinaryStatusType) == 'undefined') {
    BinaryStatusType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    BinaryStatusType.INACTIVE = {
        "value": 0,
        "css": "label-default",
        "displayName": "form.text.backOffice.status.inactive",
        "name": "Inactive"
    };
    BinaryStatusType.ACTIVE = {
        "value": 1,
        "css": "label-success",
        "displayName": "form.text.backOffice.status.active",
        "name": "Active"
    };

    (function () {
        for (atr in BinaryStatusType) {
            var obj = BinaryStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BinaryStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BinaryStatusType.values = function () {
        return objects;
    };
})();

if (typeof (NotificationSettingStatusType) == 'undefined') {
    NotificationSettingStatusType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    NotificationSettingStatusType.DELETE = {
        "value": -1,
        "css": "label-danger",
        "name": "form.text.backOffice.status.invisible",
    };
    NotificationSettingStatusType.INACTIVE = {
        "value": 0,
        "css": "label-default",
        "name": "form.text.backOffice.status.inactive",
    };
    NotificationSettingStatusType.ACTIVE = {
        "value": 1,
        "css": "label-success",
        "name": "form.text.backOffice.status.active",
    };

    (function () {
        for (atr in NotificationSettingStatusType) {
            var obj = NotificationSettingStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    NotificationSettingStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    NotificationSettingStatusType.values = function () {
        return objects;
    };
})();

if (typeof (BonusWalletType) == 'undefined') {
    BonusWalletType = {}
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    BonusWalletType.MAIN = {
        "value": -1,
        "css": "Main",
        "name": "",
    };
    BonusWalletType.PROMOTION = {
        "value": -2,
        "css": "Promotion Wallet",
        "name": "",
    };

    (function () {
        for (atr in BonusWalletType) {
            var obj = BonusWalletType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BonusWalletType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BonusWalletType.values = function () {
        return objects;
    };
})();

if (typeof (ParticipationType) == 'undefined') {
    ParticipationType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ParticipationType.AUTO = {
        "value": 1,
        "name": "Auto"
    };
    ParticipationType.SEMI_AUTO = {
        "value": 2,
        "name": "Semi-Auto"
    };
    ParticipationType.MANUAL = {
        "value": 3,
        "name": "Manual"
    };
    ParticipationType.PROMO_CODE = {
        "value": 4,
        "name": "Promo Code"
    };
    ParticipationType.SYSTEM_DYNAMICS = {
        "value": 5,
        "name": "System Dynamics"
    };


    (function () {
        for (atr in ParticipationType) {
            var obj = ParticipationType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ParticipationType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ParticipationType.values = function () {
        return objects;
    };
})();

if (typeof (ManualForceServeType) == 'undefined') {
    ManualForceServeType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    ManualForceServeType.MANUAL = {
        "value": 0,
        "name": "Manual"
    };
    ManualForceServeType.AUTO = {
        "value": 1,
        "name": "Auto"
    };
    ManualForceServeType.NOT_ALLOWED = {
        "value": -1,
        "name": "Not Allowd"
    };


    (function () {
        for (atr in ManualForceServeType) {
            var obj = ManualForceServeType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ManualForceServeType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ManualForceServeType.values = function () {
        return objects;
    };
})();

if (typeof (UserChannelType) == 'undefined') {
    UserChannelType = {}
}
(function () {
    let getValue = function () {
        return this.value;
    };
    let objects = [];

    UserChannelType.DIRECT = {
        value: 0,
        displayName: 'Direct'
    };
    UserChannelType.AFFILIATE = {
        value: 1,
        displayName: 'Affiliate'
    };

    (function () {
        for (let atr in UserChannelType) {
            let obj = UserChannelType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    UserChannelType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    UserChannelType.values = function () {
        return objects;
    };
}());

if (typeof (AccountSummaryReportType) == 'undefined') {
    AccountSummaryReportType = {}
}
(function () {
    let getValue = function () {
        return this.value;
    };
    let objects = [];

    AccountSummaryReportType.BET = {
        value: 0,
    };
    AccountSummaryReportType.DEPOSIT = {
        value: 1,
    };
    AccountSummaryReportType.ADJUSTMENT = {
        value: 2,
    };
    AccountSummaryReportType.WITHDRAWALS = {
        value: 3,
    };
    AccountSummaryReportType.TRANSFER = {
        value: 4,
    };
    AccountSummaryReportType.TURNOVER_BONUS = {
        value: 5,
    };
    AccountSummaryReportType.LOSS_BONUS = {
        value: 6,
    };
    AccountSummaryReportType.ISSUE_BONUS = {
        value: 7,
    };
    AccountSummaryReportType.TURNOVER = {
        value: 9,
    };
    AccountSummaryReportType.SPECIAL_BONUS = {
        value: 10,
    };
    AccountSummaryReportType.TRANSFER_IN = {
        value: 11,
    };
    AccountSummaryReportType.TRANSFER_OUT = {
        value: 12,
    };
    AccountSummaryReportType.DEPOSIT_BONUS = {
        value: 13,
    };
    AccountSummaryReportType.RECYCLE_BALANCE = {
        value: 14,
    };
    AccountSummaryReportType.CANCEL_FEE = {
        value: 15,
    };
    AccountSummaryReportType.DEPOSIT_FEE = {
        value: 16,
    };
    AccountSummaryReportType.WITHDRAWAL_FEE = {
        value: 17,
    };
    AccountSummaryReportType.REVENUE_ADJUSTMENT = {
        value: 18,
    };
    AccountSummaryReportType.REFERRAL_COMMISSION = {
        value: 19,
    };

    (function () {
        for (let atr in AccountSummaryReportType) {
            let obj = AccountSummaryReportType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AccountSummaryReportType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    AccountSummaryReportType.values = function () {
        return objects;
    };
})();


if (typeof (TransferStatusType) == 'undefined') {
    TransferStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    TransferStatusType.PENDING = {
        "value": 0,
        "getClassName": function () {
            return "label-default";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.transferStatusType.PENDING";
        },
    };
    TransferStatusType.SUCCESS = {
        "value": 1,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Success";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.transferStatusType.SUCCESS";
        },
    };
    TransferStatusType.FAIL = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Fail";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.transferStatusType.FAIL";
        },
    };

    (function () {
        for (atr in TransferStatusType) {
            var obj = TransferStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    TransferStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    TransferStatusType.values = function () {
        return objects;
    };
})();

if (typeof (BackOfficeStatusType) == 'undefined') {
    BackOfficeStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    BackOfficeStatusType.NEW = {
        "value": 0,
        "getClassName": function () {
            return "label-primary";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.backOfficeStatusType.PENDING";
        },
    };
    BackOfficeStatusType.PROCESSING = {
        "value": 1,
        "getClassName": function () {
            return "label-warning";
        },
        "getName": function () {
            return "Processing";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.backOfficeStatusType.PROCESSING";
        },
    };
    BackOfficeStatusType.FAILED = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Failed";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.backOfficeStatusType.FAIL";
        },
    };
    BackOfficeStatusType.COMPLETED = {
        "value": 2,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Completed";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.backOfficeStatusType.COMPLETE";
        },
    };
    BackOfficeStatusType.AWAITED = {
        "value": 3,
        "getClassName": function () {
            return "label-await";
        },
        "getName": function () {
            return "Awaited";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.backOfficeStatusType.AWAITED";
        },
    };


    (function () {
        for (atr in BackOfficeStatusType) {
            var obj = BackOfficeStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BackOfficeStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BackOfficeStatusType.values = function () {
        return objects;
    };
})();

if (typeof (ReportExportType) == 'undefined') {
    ReportExportType = {};
}
(function () {
    const getValue = function () {
        return this.value;
    };
    const objects = [];

    ReportExportType.MEMBER = {
        "value": 0,
        "getName": function () {
            return "member";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.member";
        },
    };
    ReportExportType.MEMBER_WITH_PROVIDER_ACCOUNT = {
        "value": 1,
        "getName": function () {
            return "memberProviderAccount";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.member";
        },
    };
    ReportExportType.DEPOSIT = {
        "value": 2,
        "getName": function () {
            return "deposit";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.deposit";
        },
    };
    ReportExportType.WITHDRAWAL = {
        "value": 3,
        "getName": function () {
            return "withdrawal";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.withdrawal";
        },
    };
    ReportExportType.ADJUSTMENT = {
        "value": 4,
        "getName": function () {
            return "adjustment";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.adjustment";
        },
    };
    ReportExportType.BONUS = {
        "value": 5,
        "getName": function () {
            return "bonus";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.bonus";
        },
    };
    ReportExportType.PAYMENT = {
        "value": 6,
        "getName": function () {
            return "payment";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.payment";
        },
    };
    ReportExportType.TURNOVER = {
        "value": 7,
        "getName": function () {
            return "turnover";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.turnover";
        },
    };
    ReportExportType.DAILY = {
        "value": 8,
        "getName": function () {
            return "daily";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.daily";
        },
    };
    ReportExportType.VENDOR = {
        "value": 9,
        "getName": function () {
            return "vendor";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.vendor";
        },
    };
    ReportExportType.VIP_CHANGE = {
        "value": 10,
        "getName": function () {
            return "vipChange";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.reportExportType.vipChange";
        },
    };
    ReportExportType.FRAUD_TOOL = {
        "value": 11,
        "getName": function () {
            return "fraudTool";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.fraudTools";
        },
    };
    ReportExportType.AFFILIATE = {
        "value": 12,
        "getName": function () {
            return "affiliate";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.affiliate";
        },
    };
    ReportExportType.COMMISSION_STRUCTURE = {
        "value": 13,
        "getName": function () {
            return "commissionStructure";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.commissionStructure";
        },
    };
    ReportExportType.PERFORMANCE_AFFILIATE = {
        "value": 14,
        "getName": function () {
            return "affiliatePerformance";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.performance";
        },
    };
    ReportExportType.PERFORMANCE_PLAYER = {
        "value": 15,
        "getName": function () {
            return "playerPerformance";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.performance";
        },
    };
    ReportExportType.AFFILIATE_DOWN_LINE_SETTING = {
        "value": 16,
        "getName": function () {
            return "downLineSetting";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.downLineSetting";
        },
    };
    ReportExportType.REFERRAL = {
        "value": 17,
        "getName": function () {
            return "referral";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.referral";
        },
    };
    ReportExportType.TODAY_BET = {
        "value": 18,
        "getName": function () {
            return "todayBet";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.reportExportType.todayBet";
        },
    };
    ReportExportType.TRANSFER = {
        "value": 19,
        "getName": function () {
            return "transfer";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.transfer";
        },
    };
    ReportExportType.GAME = {
        "value": 20,
        "getName": function () {
            return "game";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.game";
        },
    };
    ReportExportType.DOCUMENT = {
        "value": 21,
        "getName": function () {
            return "document";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.document";
        },
    };
    ReportExportType.DASHBOARD_BONUS = {
        "value": 22,
        "getName": function () {
            return "dashboardBonus";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardBonus";
        },
    };
    ReportExportType.DASHBOARD_REGISTER = {
        "value": 23,
        "getName": function () {
            return "dashboardRegister";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardRegister";
        },
    };
    ReportExportType.DASHBOARD_PROFIT = {
        "value": 24,
        "getName": function () {
            return "dashboardCompanyProfit";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardCompanyProfit";
        },
    };
    ReportExportType.DASHBOARD_DEPOSIT = {
        "value": 25,
        "getName": function () {
            return "dashboardDeposit";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardDeposit";
        },
    };
    ReportExportType.DASHBOARD_WITHDRAWAL = {
        "value": 26,
        "getName": function () {
            return "dashboardWithdrawal";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardWithdrawal";
        },
    };
    ReportExportType.DASHBOARD_FIRST_DEPOSIT = {
        "value": 27,
        "getName": function () {
            return "dashboardFirstDeposit";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardFirstDeposit";
        },
    };
    ReportExportType.DASHBOARD_TURNOVER = {
        "value": 28,
        "getName": function () {
            return "dashboardTurnover";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardTurnover";
        },
    };
    ReportExportType.DASHBOARD_RECYCLE_BALANCE = {
        "value": 29,
        "getName": function () {
            return "dashboardRecycleBalance";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.dashboardRecycleBalance";
        },
    };
    ReportExportType.VIP_POINT_EARN = {
        "value": 31,
        "getName": function () {
            return "vipPointEarn";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.report.vip.point.earn";
        },
    };
    ReportExportType.MEMBER_GROUP_DETAIL = {
        "value": 33,
        "getName": function () {
            return "memberGroupDetail";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.memberGroupDetail";
        },
    };
    ReportExportType.AFFILIATE_FINANCE = {
        "value": 35,
        "getName": function () {
            return "affiliateFinance";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.affiliateFinance";
        },
    };
    ReportExportType.AFFILIATE_FINANCE_DOWNLINE = {
        "value": 36,
        "getName": function () {
            return "affiliateFinanceDownline";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.affiliateFinanceDownline";
        },
    };
    ReportExportType.SEMI_AUTO_TURNOVER_DETAIL = {
        "value": 38,
        "getName": function () {
            return "semiAutoTurnoverDetail";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.semiAutoBonusDetail";
        },
    };
    ReportExportType.SEMI_AUTO_LOSS_DETAIL = {
        "value": 39,
        "getName": function () {
            return "semiAutoLossDetail";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.semiAutoBonusDetail";
        },
    };
    ReportExportType.PAYMENT_DEPOSIT = {
        "value": 40,
        "getName": function () {
            return "paymentDeposit";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.payment";
        },
    };
    ReportExportType.PAYMENT_WITHDRAWAL = {
        "value": 41,
        "getName": function () {
            return "paymentWithdrawal";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.payment";
        },
    };
    ReportExportType.PAYMENT_ADJUSTMENT = {
        "value": 42,
        "getName": function () {
            return "paymentAdjustment";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.payment";
        },
    };
    ReportExportType.AFFILIATE_DOMAIN = {
        "value": 44,
        "getName": function () {
            return "affiliateDomain";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.affiliateDomain";
        },
    };
    ReportExportType.PAYMENT_ACCOUNT = {
        "value": 45,
        "getName": function () {
            return "paymentAccount";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.paymentAccount";
        },
    };
    ReportExportType.REFERRAL_COMMISSION_VERIFY = {
        "value": 46,
        "getName": function () {
            return "referralCommissionVerify";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.commissionVerify";
        },
    };
    ReportExportType.REFERRAL_COMMISSION_REPORT = {
        "value": 47,
        "getName": function () {
            return "referralCommissionReport";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.commissionReport";
        },
    };
    ReportExportType.REFERRAL_COMMISSION_DOWNLINE_REPORT = {
        "value": 48,
        "getName": function () {
            return "referralCommissionDownLineReport";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.commissionDownLineDetailReport";
        },
    };
    ReportExportType.PROFILE_COMMISSION_REPORT = {
        "value": 49,
        "getName": function () {
            return "profileReferralCommissionReport";
        },
        "getDisplayName": function () {
            return "form.text.backOffice.breadcrumbs.profileCommissionReport";
        },
    };

    for (atr in ReportExportType) {
        var obj = ReportExportType[atr];
        objects[objects.length] = obj;
        obj.name = atr;
        obj.unique = getValue;
    }
    ReportExportType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    ReportExportType.values = function () {
        return objects;
    };
})();

if (typeof (AccountGenderType) == 'undefined') {
    AccountGenderType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AccountGenderType.FEMALE = {
        "value": 0,
        "getName": function () {
            return "Female";
        },
        "getFullName": function () {
            return "fe.text.profile.genderType.female";
        }
    };
    AccountGenderType.MALE = {
        "value": 1,
        "getName": function () {
            return "Male";
        },
        "getFullName": function () {
            return "fe.text.profile.genderType.male";
        }
    };

    (function () {
        for (atr in AccountGenderType) {
            var obj = AccountGenderType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AccountGenderType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AccountGenderType.values = function () {
        return objects;
    };

})();

if (typeof (AccountMaritalType) == 'undefined') {
    AccountMaritalType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    AccountMaritalType.SINGLE = {
        "value": 0,
        "getName": function () {
            return "Single";
        },
        "getFullName": function () {
            return "fe.text.profile.maritalType.single";
        }
    };
    AccountMaritalType.MARRIED = {
        "value": 1,
        "getName": function () {
            return "Married";
        },
        "getFullName": function () {
            return "fe.text.profile.maritalType.married";
        }
    };
    AccountMaritalType.WIDOWED = {
        "value": 2,
        "getName": function () {
            return "Widowed";
        },
        "getFullName": function () {
            return "fe.text.profile.maritalType.widowed";
        }
    };
    AccountMaritalType.DIVORCED = {
        "value": 3,
        "getName": function () {
            return "Annulled/Divorced";
        },
        "getFullName": function () {
            return "fe.text.profile.maritalType.divorced";
        }
    };

    (function () {
        for (atr in AccountMaritalType) {
            var obj = AccountMaritalType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    AccountMaritalType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    AccountMaritalType.values = function () {
        return objects;
    };

})();

if (typeof (DocumentGroupType) == 'undefined') {
    DocumentGroupType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    DocumentGroupType.DOCUMENT = {
        "value": 0,
        "getName": function () {
            return "Document";
        }
    };

    DocumentGroupType.BANK = {
        "value": 1,
        "getName": function () {
            return "Bank";
        }
    };

    DocumentGroupType.EWALLET = {
        "value": 3,
        "getName": function () {
            return "E-Wallet";
        }
    };

    DocumentGroupType.UPI = {
        "value": 4,
        "getName": function () {
            return "UPI";
        }
    };

    (function () {
        for (atr in DocumentGroupType) {
            var obj = DocumentGroupType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    DocumentGroupType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    DocumentGroupType.values = function () {
        return objects;
    };
})();

if (typeof (DocumentStatusType) == 'undefined') {
    DocumentStatusType = {};
}
(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    // DocumentStatusType.FILL_IN = {
    // 	"value": 0,
    // 	"getClassName": function() {
    // 		return "label-default";
    // 	},
    // 	"getName": function() {
    // 		return "Fill in";
    // 	},
    // 	"getDisplayName": function() {
    // 		return "global.text.documentStatusType.FILL_IN";
    // 	},
    // };

    DocumentStatusType.PENDING = {
        "value": 1,
        "getClassName": function () {
            return "label-primary";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "global.text.documentStatusType.PENDING";
        },
    };

    DocumentStatusType.APPROVED = {
        "value": 2,
        "getClassName": function () {
            return "label-success";
        },
        "getName": function () {
            return "Approved";
        },
        "getDisplayName": function () {
            return "global.text.documentStatusType.APPROVED";
        },
    };

    DocumentStatusType.ON_HOLD = {
        "value": 3,
        "getClassName": function () {
            return "label-hold";
        },
        "getName": function () {
            return "On Hold";
        },
        "getDisplayName": function () {
            return "global.text.documentStatusType.ON_HOLD";
        },
    };

    DocumentStatusType.OTP_PENDING = {
        "value": 4,
        "getClassName": function () {
            return "label-primary";
        },
        "getName": function () {
            return "Pending";
        },
        "getDisplayName": function () {
            return "global.text.documentStatusType.PENDING";
        },
    };

    DocumentStatusType.REJECTED = {
        "value": -1,
        "getClassName": function () {
            return "label-danger";
        },
        "getName": function () {
            return "Rejected";
        },
        "getDisplayName": function () {
            return "global.text.documentStatusType.REJECTED";
        },
    };

    DocumentStatusType.REMOVED = {
        "value": -2,
        "getClassName": function () {
            return "label-revert";
        },
        "getName": function () {
            return "Removed";
        },
        "getDisplayName": function () {
            return "global.text.documentStatusType.REMOVED";
        },
    };


    (function () {
        for (atr in DocumentStatusType) {
            var obj = DocumentStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    DocumentStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    DocumentStatusType.values = function () {
        return objects;
    };
})();

/*
 *
 */
if (typeof (BankTransferType) == 'undefined') {
    BankTransferType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    (function () {
        for (atr in BankTransferType) {
            var obj = BankTransferType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BankTransferType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    BankTransferType.values = function () {
        return objects;
    };
})();

/*
 *
 */
if (typeof (RemarkTemplateType) == 'undefined') {
    RemarkTemplateType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    RemarkTemplateType.DEPOSIT = {
        "value": 1,
        "name": "DEPOSIT",
        "getName": function () {
            return "Deposit";
        },
    };
    RemarkTemplateType.WITHDRAWAL = {
        "value": 2,
        "name": "WITHDRAWAL",
        "getName": function () {
            return "Withdrawal";
        },
    };
    RemarkTemplateType.ADJUSTMENT = {
        "value": 3,
        "name": "ADJUSTMENT",
        "getName": function () {
            return "Adjustment";
        },
    };
    RemarkTemplateType.FORCE_SERVE = {
        "value": 4,
        "name": "FORCE_SERVE",
        "getName": function () {
            return "Force Serve";
        },
    };
    RemarkTemplateType.MANUAL_FORCE_SERVE = {
        "value": 5,
        "name": "MANUAL_FORCE_SERVE",
        "getName": function () {
            return "Manual Force Serve";
        },
    };
    RemarkTemplateType.DOCUMENT = {
        "value": 6,
        "name": "DOCUMENT",
        "getName": function () {
            return "Document";
        },
    };
    RemarkTemplateType.RISK_REMARK = {
        "value": 7,
        "name": "RISK_REMARK",
        "getName": function () {
            return "Risk Remark";
        },
    };


    (function () {
        for (atr in RemarkTemplateType) {
            var obj = RemarkTemplateType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    RemarkTemplateType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    RemarkTemplateType.values = function () {
        return objects;
    };
})();

/*
	websiteCurrencySettingType
 */

if (typeof (WebsiteCurrencySettingType) == 'undefined') {
    WebsiteCurrencySettingType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    WebsiteCurrencySettingType.LANGUAGE = {
        "value": 2,
        "name": "LANGUAGE",
        "getName": function () {
            return "Language";
        },
    };
    WebsiteCurrencySettingType.COUNTRY = {
        "value": 3,
        "name": "COUNTRY",
        "getName": function () {
            return "Country";
        },
    };
    WebsiteCurrencySettingType.TIMEZONE = {
        "value": 4,
        "name": "TIMEZONE",
        "getName": function () {
            return "Timezone";
        },
    };
    WebsiteCurrencySettingType.MARKETINGGROUP = {
        "value": 5,
        "name": "MARKETINGGROUP",
        "getName": function () {
            return "Marketing Group";
        },
    };

    (function () {
        for (atr in WebsiteCurrencySettingType) {
            var obj = WebsiteCurrencySettingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WebsiteCurrencySettingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    WebsiteCurrencySettingType.values = function () {
        return objects;
    };
})();


/*
	websiteCountrySettingType
 */

if (typeof (WebsiteCountrySettingType) == 'undefined') {
    WebsiteCountrySettingType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    WebsiteCountrySettingType.CURRENCY = {
        "value": 1,
        "name": "CURRENCY",
        "getName": function () {
            return "Currency";
        },
    };

    (function () {
        for (atr in WebsiteCountrySettingType) {
            var obj = WebsiteCountrySettingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WebsiteCountrySettingType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    WebsiteCountrySettingType.values = function () {
        return objects;
    };
})();
/**
 *
 */
if (typeof (EngagementType) == 'undefined') {
    EngagementType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };

    EngagementType.EMAIL = {
        "value": 1,
        "name": "EMAIL",
    };
    EngagementType.SMS = {
        "value": 2,
        "name": "SMS",
    };

    (function () {
        for (let atr in EngagementType) {
            let obj = EngagementType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    EngagementType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    EngagementType.values = function () {
        return objects;
    };
})();

/**
 *
 */
if (typeof (PhoneActionType) == 'undefined') {
    PhoneActionType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };

    PhoneActionType.winic = {
        "value": 1,
        "name": "winic",
    };
    PhoneActionType.twilio = {
        "value": 2,
        "name": "twilio",
    };
    PhoneActionType.movider = {
        "value": 3,
        "name": "movider",
    };
    PhoneActionType.accessyou = {
        "value": 4,
        "name": "accessyou",
    };
    PhoneActionType.mshastra = {
        "value": 5,
        "name": "mshastra",
    };
    PhoneActionType.nexmo = {
        "value": 6,
        "name": "nexmo",
    };
    PhoneActionType.acestar = {
        "value": 7,
        "name": "acestar",
    };
    PhoneActionType.fireMobile = {
        "value": 8,
        "name": "fireMobile",
    };
    PhoneActionType.swagger = {
        "value": 9,
        "name": "swagger",
    };
    PhoneActionType.maxzino = {
        "value": 10,
        "name": "maxzino",
    };
    PhoneActionType.clickatell = {
        "value": 11,
        "name": "clickatell",
    };
    PhoneActionType.cheapGlobal = {
        "value": 12,
        "name": "cheapGlobal",
    };
    PhoneActionType.leyun = {
        "value": 13,
        "name": "leyun",
    };
    PhoneActionType.busyBee = {
        "value": 14,
        "name": "busyBee",
    };
    PhoneActionType.msg91 = {
        "value": 15,
        "name": "msg91",
    };
    PhoneActionType.karix = {
        "value": 16,
        "name": "karix",
    };
    PhoneActionType.plasgate = {
        "value": 17,
        "name": "plasgate",
    };

    (function () {
        for (let atr in PhoneActionType) {
            let obj = PhoneActionType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PhoneActionType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    PhoneActionType.values = function () {
        return objects;
    };
})();

if (typeof (VipExperienceRetentionType) == 'undefined') {
    VipExperienceRetentionType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    VipExperienceRetentionType.EXPERIENCE = {
        "value": 0,
        "getName": function () {
            return "experience";
        }
    };

    VipExperienceRetentionType.DEPOSIT = {
        "value": 1,
        "getName": function () {
            return "deposit";
        }
    };

    (function () {
        for (atr in VipExperienceRetentionType) {
            var obj = VipExperienceRetentionType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    VipExperienceRetentionType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    VipExperienceRetentionType.values = function () {
        return objects;
    };
})();

if (typeof (MoneyTransactionProcessingStatusType) == 'undefined') {
    MoneyTransactionProcessingStatusType = {};
}

(function () {
    var getValue = function () {
        return this.value;
    };
    var objects = [];

    MoneyTransactionProcessingStatusType.MANUAL = {
        "value": 1,
        "name": "Manual",
        "moneyTransactionType": 1,
        "getFullName": function () {
            return "global.text.moneyTransactionProcessingStatusType.manual";
        },
    };
    MoneyTransactionProcessingStatusType.PG = {
        "value": 2,
        "name": "PG",
        "moneyTransactionType": 4,
        "getFullName": function () {
            return "global.text.moneyTransactionProcessingStatusType.pg";
        },
    };

    (function () {
        for (atr in MoneyTransactionProcessingStatusType) {
            var obj = MoneyTransactionProcessingStatusType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    MoneyTransactionProcessingStatusType.getInstanceOf = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["value"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MoneyTransactionProcessingStatusType.getInstanceByMoneyTransactionType = function (value) {
        for (var i = 0; i < objects.length; i++) {
            if (objects[i]["moneyTransactionType"] == value) {
                return objects[i];
            }
        }
        return null;
    };

    MoneyTransactionProcessingStatusType.values = function () {
        return objects;
    };
})();

/**
 *
 */
if (typeof (ExternalProviderType) == 'undefined') {
    ExternalProviderType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };

    ExternalProviderType.FANTASTIC = {
        "value": 0,
        "name": "Fantastic",
    };
    ExternalProviderType.NINE_WICKETS = {
        "value": 1,
        "name": "9wickets",
    };

    (function () {
        for (let atr in ExternalProviderType) {
            let obj = ExternalProviderType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    ExternalProviderType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    ExternalProviderType.values = function () {
        return objects;
    };
})();

/**
 *
 */
if (typeof (BonusReceiveType) == 'undefined') {
    BonusReceiveType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };
    BonusReceiveType.BONUS = {
        "value": 1,
        "name": "Bonus",
    };

    (function () {
        for (let atr in BonusReceiveType) {
            let obj = BonusReceiveType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    BonusReceiveType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    BonusReceiveType.values = function () {
        return objects;
    };
})();
/**
 *
 */
if (typeof (WebsiteCategoryType) == 'undefined') {
    WebsiteCategoryType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };

    WebsiteCategoryType.CATEGORY_CONTAINER = {
        "value": 0,
        "name": "CATEGORY_CONTAINER",
        "layer": 2
    };
    WebsiteCategoryType.GAME_CONTAINER = {
        "value": 1,
        "name": "Game Container",
        "layer": 1
    };
    WebsiteCategoryType.DIRECT_URL = {
        "value": 2,
        "name": "Direct URL",
        "layer": 1
    };
    WebsiteCategoryType.VENDOR = {
        "value": 3,
        "name": "Vendor",
        "layer": 2
    };
    WebsiteCategoryType.GAME = {
        "value": 4,
        "name": "Game",
        "layer": 1
    };


    (function () {
        for (let atr in WebsiteCategoryType) {
            let obj = WebsiteCategoryType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    WebsiteCategoryType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    WebsiteCategoryType.values = function () {
        return objects;
    };
})();

/**
 *
 */
if (typeof (TurnoverGroupingType) == 'undefined') {
    TurnoverGroupingType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };

    TurnoverGroupingType.USER_ID = {
        "value": 1,
        "name": "CATEGORY_CONTAINER",
    };
    TurnoverGroupingType.GAME_TYPR = {
        "value": 2,
        "name": "Game Container",
    };
    TurnoverGroupingType.GAME = {
        "value": 3,
        "name": "Direct URL",
    };

    (function () {
        for (let atr in TurnoverGroupingType) {
            let obj = TurnoverGroupingType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    TurnoverGroupingType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    TurnoverGroupingType.values = function () {
        return objects;
    };
})();

/**
 *
 */
if (typeof (PersonalInfoType) == 'undefined') {
    PersonalInfoType = {};
}

(function () {

    let objects = [];

    let getValue = function () {
        return this.value;
    };

    PersonalInfoType.BIRTHDAY = {
        "value": 1,
        "name": "birthday",
        "idName": "birthday"
    };
    PersonalInfoType.GENDER = {
        "value": 2,
        "name": "gender",
        "idName": "gender"
    };
    PersonalInfoType.MARITAL = {
        "value": 4,
        "name": "marital",
        "idName": "marital"
    };
    PersonalInfoType.FULL_NAME = {
        "value": 8,
        "name": "full name",
        "idName": "fullName"
    };

    (function () {
        for (let atr in PersonalInfoType) {
            let obj = PersonalInfoType[atr];
            objects[objects.length] = obj;
            obj.name = atr;
            obj.unique = getValue;
        }
    })();

    PersonalInfoType.getInstanceOf = function (value) {
        for (let i = 0; i < objects.length; i++) {
            if (objects[i]["value"] === value) {
                return objects[i];
            }
        }
        return null;
    };

    WebsiteCategoryType.values = function () {
        return objects;
    };
})();