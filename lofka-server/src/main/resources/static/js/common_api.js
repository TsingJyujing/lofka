/**
 * Created by YuanYifan on 2017/6/28.
 */

$.extend({
    getUrlVars: function () {
        var vars = [],
            hash;
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for (var i = 0; i < hashes.length; i++) {
            hash = hashes[i].split('=');
            vars.push(hash[0]);
            vars[hash[0]] = hash[1];
        }
        return vars;
    },
    getUrlVar: function (name) {
        return $.getUrlVars()[name];
    }
});

function leave_point(num, point_size) {
    return Math.round(Math.pow(10, point_size) * num) / Math.pow(10, point_size);
}

function is_defined(x) {
    return !(x === undefined);
}

var range = function (start, stop, step) {
    if (!is_defined(stop)) {
        stop = start;
        start = 0;
    }
    if (!is_defined(step) || step == 0)
        step = 1;
    out = new Array();
    if (step > 0) {
        for (var i = start; i < stop; i += step)
            out.push(i);
    } else {
        for (var i = start; i > stop; i += step)
            out.push(i);
    }
    return out;
};

var len = function (obj) {
    var l = 0;
    switch (typeof(obj)) {
        case 'array':
        case 'string':
            return obj.length;
        case 'object':
            for (var key in obj)
                l++;
            return l;
        default:
            return 1;
    }
};

function str(obj) {
    if (!is_defined(obj)) {
        return "undefined";
    } else if (is_defined(obj.__str__)) {
        return obj.__str__();
    } else if (typeof(obj) == "number") {
        return String(obj);
    } else if (typeof(obj) == "string") {
        return obj;
    } else {
        return repr(obj);
    }
}


sprintfWrapper = {
    init: function () {
        if (!is_defined(arguments))
            return null;
        if (arguments.length < 1)
            return null;
        if (typeof(arguments[0]) != "string")
            return null;
        if (!is_defined(RegExp))
            return null;

        var string = arguments[0];
        var exp = new RegExp(/(%([%]|(\-)?(\+|\x20)?(0)?(\d+)?(\.(\d)?)?([bcdfosxX])))/g);
        var matches = new Array();
        var strings = new Array();
        var convCount = 0;
        var stringPosStart = 0;
        var stringPosEnd = 0;
        var matchPosEnd = 0;
        var newString = '';
        var match = null;

        while ((match = exp.exec(string))) {
            if (match[9])
                convCount += 1;

            stringPosStart = matchPosEnd;
            stringPosEnd = exp.lastIndex - match[0].length;
            strings[strings.length] = string.substring(stringPosStart, stringPosEnd);

            matchPosEnd = exp.lastIndex;
            matches[matches.length] = {
                match: match[0],
                left: match[3] ? true : false,
                sign: match[4] || '',
                pad: match[5] || ' ',
                min: match[6] || 0,
                precision: match[8],
                code: match[9] || '%',
                negative: parseInt(arguments[convCount]) < 0 ? true : false,
                argument: String(arguments[convCount])
            };
        }
        strings[strings.length] = string.substring(matchPosEnd);

        if (matches.length == 0)
            return string;
        if ((arguments.length - 1) < convCount)
            return null;

        var code = null;
        var match = null;
        var i = null;

        for (i = 0; i < matches.length; i++) {
            if (matches[i].code == '%') {
                substitution = '%'
            }
            else if (matches[i].code == 'b') {
                matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(2));
                substitution = sprintfWrapper.convert(matches[i], true);
            }
            else if (matches[i].code == 'c') {
                matches[i].argument = String(String.fromCharCode(parseInt(Math.abs(parseInt(matches[i].argument)))));
                substitution = sprintfWrapper.convert(matches[i], true);
            }
            else if (matches[i].code == 'd') {
                matches[i].argument = String(Math.abs(parseInt(matches[i].argument)));
                substitution = sprintfWrapper.convert(matches[i]);
            }
            else if (matches[i].code == 'f') {
                matches[i].argument = String(Math.abs(parseFloat(matches[i].argument)).toFixed(matches[i].precision ? matches[i].precision : 6));
                substitution = sprintfWrapper.convert(matches[i]);
            }
            else if (matches[i].code == 'o') {
                matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(8));
                substitution = sprintfWrapper.convert(matches[i]);
            }
            else if (matches[i].code == 's') {
                matches[i].argument = matches[i].argument.substring(0, matches[i].precision ? matches[i].precision : matches[i].argument.length)
                substitution = sprintfWrapper.convert(matches[i], true);
            }
            else if (matches[i].code == 'x') {
                matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(16));
                substitution = sprintfWrapper.convert(matches[i]);
            }
            else if (matches[i].code == 'X') {
                matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(16));
                substitution = sprintfWrapper.convert(matches[i]).toUpperCase();
            }
            else {
                substitution = matches[i].match;
            }

            newString += strings[i];
            newString += substitution;
        }

        newString += strings[i];
        return newString;
    },
    convert: function (match, nosign) {
        if (nosign)
            match.sign = '';
        else
            match.sign = match.negative ? '-' : match.sign;

        var l = match.min - match.argument.length + 1 - match.sign.length;
        var pad = new Array(l < 0 ? 0 : l).join(match.pad);
        if (!match.left) {
            if (match.pad == "0" || nosign)
                return match.sign + pad + match.argument;
            else
                return pad + match.sign + match.argument;
        } else {
            if (match.pad == "0" || nosign)
                return match.sign + match.argument + pad.replace(/0/g, ' ');
            else
                return match.sign + match.argument + pad;
        }
    }
};


sprintf = sprintfWrapper.init;

String.prototype.sprintf = function () {
    var args = new Array(this.toString());
    var params = Array.prototype.slice.call(arguments);
    for (var param in params) {
        args.push(params[param]);
    }
    return sprintfWrapper.init.apply(this, args);
}

String.prototype.lower = String.prototype.toLowerCase;
String.prototype.upper = String.prototype.toUpperCase;
String.prototype.update = String.prototype.extend;

String.prototype.startswith = function (s) {
    return this.slice(0, s.length) == s;
};
String.prototype.endswith = function (s) {
    return this.slice(this.length - s.length) == s;
};
String.prototype.encode = function (encoding) {
    encoding = encoding.toLowerCase();
    if (encoding == "utf8" || encoding == "utf-8")
        return utf8encode(this);
    throw Error("Unknown encoding: " + encoding);
};

/**
 * A.insert(index, object) -- insert object before index
 */
Array.prototype.insert = function (index, object) {
    this.splice(index, 0, object);
};
/**
 * A.append(object) -- append object to array
 */

Array.prototype.append = function (object) {
    this[this.length] = object;
};

/**
 * A.pop([index]) -> item -- remove and return item at index (default last).
 * Returns undefined if list is empty or index is out of range.
 */
Array.prototype.pop = function (index) {
    if (!is_defined(index))
        index = this.length - 1;
    if (index == -1 || index >= this.length)
        return undefined;
    var elt = this[index];
    this.splice(index, 1);
    return elt;
};

Array.prototype.sum = function () {
    return this.reduce(
        function (a, b) {
            return a + b;
        }
    )
};

Array.prototype.sum2 = function () {
    return this.map(
        function (val) {
            return val.sum()
        }
    ).sum();
};

Array.prototype.standarized = function (scale) {
    if (scale === undefined) {
        scale = 1;
    }
    var sum_value = this.reduce(
        function (a, b) {
            return a + b;
        }
    );
    return this.map(
        function (value) {
            return scale * value / sum_value
        }
    )
};

Array.prototype.map2 = function (f) {
    return this.map(
        function (val_array) {
            return val_array.map(
                function (val) {
                    return f(val);
                }
            )
        }
    )
};

Array.prototype.reverse_findIndex = function (fun) {
    for (var i = this.length - 1; i >= 0; i--) {
        if (fun(this[i])) {
            return i;
        }
    }
    return -1
};

Array.prototype.maxIndex = function () {
    if (this.length <= 0) {
        return -1;
    }
    var maxValue = this[0];
    var maxIndexRet = 0;
    for (var i = 1; i < this.length; i++) {
        if (this[i] >= maxValue) {
            maxValue = this[i];
            maxIndexRet = i;
        }
    }
    return maxIndexRet;
};


Array.prototype.minIndex = function () {
    if (this.length <= 0) {
        return -1;
    }
    var minValue = this[0];
    var minIndexRet = 0;
    for (var i = 1; i < this.length; i++) {
        if (this[i] < minValue) {
            minValue = this[i];
            minIndexRet = i;
        }
    }
    return minIndexRet;
};

Array.prototype.max = function () {
    if (this.length <= 0) {
        throw Exception("Require max in an empty array!");
    }
    var maxValue = this[0];
    for (var i = 1; i < this.length; i++) {
        if (this[i] >= maxValue) {
            maxValue = this[i];
        }
    }
    return maxValue;
};


Array.prototype.min = function () {
    if (this.length <= 0) {
        throw Exception("Require min in an empty array!");
    }
    var minValue = this[0];
    for (var i = 1; i < this.length; i++) {
        if (this[i] < minValue) {
            minValue = this[i];
        }
    }
    return minValue;
};


function initTableCheckbox() {
    var $thr = $("#analysis_items_div").find('table thead tr');
    var $checkAllTh = $('<th><input type="checkbox" id="checkAll" name="checkAll" checked="checked"/></th>');
    /*将全选/反选复选框添加到表头最前，即增加一列*/
    $thr.prepend($checkAllTh);
    /*“全选/反选”复选框*/
    var $checkAll = $thr.find('input');
    $checkAll.click(function (event) {
        /*将所有行的选中状态设成全选框的选中状态*/
        $tbr.find('input').prop('checked', $(this).prop('checked'));
        /*并调整所有选中行的CSS样式*/
        if ($(this).prop('checked')) {
            $tbr.find('input').parent().parent().addClass('warning');
        } else {
            $tbr.find('input').parent().parent().removeClass('warning');
        }
        /*阻止向上冒泡，以防再次触发点击操作*/
        event.stopPropagation();
    });
    /*点击全选框所在单元格时也触发全选框的点击操作*/
    $checkAllTh.click(function () {
        $(this).find('input').click();
    });
    var $tbr = $("#analysis_items_div").find('table tbody tr');

    /*每一行都在最前面插入一个选中复选框的单元格*/
    $tbr.each(
        function () {
            $(this).prepend(
                $('<td></td>').append(
                    $("<input>").attr({
                        type: "checkbox",
                        name: "checkItem",
                        spec_type: "analysis_item",
                        id: "checkItem_" + $(this).attr("id"),
                        checked: "checked"
                    })
                )
            );
        }
    );

    /*点击每一行的选中复选框时*/
    $tbr.find('input').click(function (event) {
        /*调整选中行的CSS样式*/
        $(this).parent().parent().toggleClass('warning');
        /*如果已经被选中行的行数等于表格的数据行数，将全选框设为选中状态，否则设为未选中状态*/
        $checkAll.prop('checked', $tbr.find('input:checked').length == $tbr.length ? true : false);
        /*阻止向上冒泡，以防再次触发点击操作*/
        event.stopPropagation();
    });
    /*点击每一行时也触发该行的选中操作*/
    $tbr.click(function () {
        $(this).find('input').click();
    });
}


//判断浏览区是否支持canvas
function isSupportCanvas() {
    const elem = document.createElement('canvas');
    return !!(elem.getContext && elem.getContext('2d'));
}

function assert(bCondition, sErrorMsg) {
    if (!bCondition) {
        alert(sErrorMsg);
        throw new Error(sErrorMsg);
    }
}