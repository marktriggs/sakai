GbGradeTable = {};

GbGradeTable.unpack = function (s, rowCount, columnCount) {
  var blob = atob(s);

  // Our result will be an array of Float64Array rows
  var result = [];

  // The byte from our blob we're currently working on
  var readIndex = 0;

  for (var row = 0; row < rowCount; row++) {
    var writeIndex = 0;
    var currentRow = [];

    for (var column = 0; column < columnCount; column++) {
      if (blob[readIndex].charCodeAt() == 127) {
        // This is a sentinel value meaning "null"
        currentRow[writeIndex] = "";
	readIndex += 1;
      } else if (blob[readIndex].charCodeAt() & 128) {
        // If the top bit is set, we're reading a two byte integer
        currentRow[writeIndex] = (((blob[readIndex].charCodeAt() & 63) << 8) | blob[readIndex + 1].charCodeAt());

        // If the second-from-left bit is set, there's a fraction too
        if (blob[readIndex].charCodeAt() & 64) {
	  // third byte is a fraction
	  var fraction = blob[readIndex + 2].charCodeAt();
	  currentRow[writeIndex] += (fraction / Math.pow(10, Math.ceil(Math.log10(fraction))));
	  readIndex += 1;
        }

        readIndex += 2;
      } else {
        // a one byte integer and no fraction
        currentRow[writeIndex] = blob[readIndex].charCodeAt();
        readIndex += 1;
      }

      writeIndex += 1;
    };

    result.push(currentRow);
  }

  return result;
};

$(document).ready(function() {
  // need TrimPath to load before parsing templates
  GbGradeTable.templates = {
    cell: TrimPath.parseTemplate(
        $("#cellTemplate").html().trim().toString()),
    courseGradeCell: TrimPath.parseTemplate(
        $("#courseGradeCellTemplate").html().trim().toString()),
    courseGradeHeader: TrimPath.parseTemplate(
        $("#courseGradeHeaderTemplate").html().trim().toString()),
    assignmentHeader: TrimPath.parseTemplate(
        $("#assignmentHeaderTemplate").html().trim().toString()),
    categoryAverageHeader: TrimPath.parseTemplate(
        $("#categoryAverageHeaderTemplate").html().trim().toString()),
    studentHeader: TrimPath.parseTemplate(
        $("#studentHeaderTemplate").html().trim().toString()),
    studentCell: TrimPath.parseTemplate(
        $("#studentCellTemplate").html().trim().toString())
  };

});

GbGradeTable.courseGradeRenderer = function (instance, td, row, col, prop, value, cellProperties) {

  var $td = $(td);
  var cellKey = (row + ',' + col + ',' + value);
  var wasInitialised = $td.data('cell-initialised');

  if (wasInitialised === cellKey) {
    return;
  }

  if (!wasInitialised) {
    var html = GbGradeTable.templates.courseGradeCell.process({
      value: value
    });

    td.innerHTML = html;
  } else if (wasInitialised != cellKey) {
    $td.find(".gb-value").html(value);
  }

  $td.data('cell-initialised', cellKey);
};

GbGradeTable.cellRenderer = function (instance, td, row, col, prop, value, cellProperties) {

  var $td = $(td);
  var index = col - 1;
  var cellKey = (row + ',' + index);

  var wasInitialised = $td.data('cell-initialised');

  if (wasInitialised === cellKey) {
    // Nothing to do
    return;
  }

  var column = GbGradeTable.columns[index];
  var student = GbGradeTable.students[row];

  // THINKME: All of this was here because patching the DOM was faster than
  // replacing innerHTML on every scroll event.  Can we do the same sort of
  // thing?
  if (!wasInitialised) {
    // First time we've initialised this cell.
    var html = GbGradeTable.templates.cell.process({
      value: value
    });

    td.innerHTML = html;
  } else if (wasInitialised != cellKey) {
    // This cell was previously holding a different value.  Just patch it.
    $td.find(".gb-value").html(value);
  }

  $td.data("studentid", student.userId);
  if (column.type === "assignment") {
    $td.data("assignmentid", column.assignmentId);
    $td.removeData("categoryId");
  } else if (column.type === "category") {
    $td.data("categoryId", column.categoryId);
    $td.removeData("assignmentid");
    if (value != null && (value+"").length > 0) {
      $td.find(".gb-value").append("%");
    } else {
      $td.find(".gb-value").html("-");
    }
  } else {
    throw "column.type not supported: " + column.type;
  }

  $td.data('cell-initialised', cellKey);
};


GbGradeTable.headerRenderer = function (col) {
  if (col == 0) {
    return GbGradeTable.templates.courseGradeHeader.process();
  }

  var column =  GbGradeTable.columns[col - 1];

  if (column.type === "assignment") {
    return GbGradeTable.templates.assignmentHeader.process(column);
  } else if (column.type === "category") {
    return GbGradeTable.templates.categoryAverageHeader.process(column);
  } else {
    return "Unknown column type for column: " + col + " (" + column.type+ ")";
  }
};

GbGradeTable.studentCellRenderer = function(row) {
  return GbGradeTable.templates.studentCell.process(GbGradeTable.students[row]);
}


GbGradeTable.mergeColumns = function (data, extraColumns) {
  var result = [];

  for (var row = 0; row < data.length; row++) {
    var updatedRow = []
    for (var col = 0; col < data[row].length; col++) {
      if (extraColumns[col]) {
        updatedRow.push(extraColumns[col][row]);
      }

      updatedRow.push(data[row][col]);
    }

    result.push(updatedRow)
  }

  return result;
}

// FIXME: Hard-coded stuff here
GbGradeTable.renderTable = function (elementId, tableData) {
  GbGradeTable.students = tableData.students;
  GbGradeTable.columns = tableData.columns;
  GbGradeTable.grades = GbGradeTable.mergeColumns(GbGradeTable.unpack(tableData.serializedGrades,
                                                                      tableData.rowCount,
                                                                      tableData.columnCount),
                                                  {
                                                    0: tableData.courseGrades,
                                                  });

  GbGradeTableEditor = Handsontable.editors.TextEditor.prototype.extend();

  GbGradeTableEditor.prototype.createElements = function () {
    Handsontable.editors.TextEditor.prototype.createElements.apply(this, arguments);
    // add 'out-of' label
    var outOf = "<span class='out-of'>/10</span>";
    $(this.TEXTAREA_PARENT).append(outOf);
  };

  GbGradeTableEditor.prototype.beginEditing = function() {
    Handsontable.editors.TextEditor.prototype.beginEditing.apply(this, arguments);
    if ($(this.TEXTAREA).val().length > 0) {
      $(this.TEXTAREA).select();
    }
  };

  GbGradeTableEditor.prototype.saveValue = function() {
    Handsontable.editors.TextEditor.prototype.saveValue.apply(this, arguments);
    console.log("-- SAVING --");
    console.log("value: " + $(this.TEXTAREA).val());
    console.log("studentId: " + $(this.TD).data("studentid"));
    console.log("assignmentId: " + $(this.TD).data("assignmentid"));
    // TODO ajax post and add notifications to this.TD for success/error
  }


  GbGradeTable.instance = new Handsontable(document.getElementById(elementId), {
    data: GbGradeTable.grades,
    rowHeaderWidth: 220,
    rowHeaders: GbGradeTable.studentCellRenderer,
    fixedColumnsLeft: 1,
    colHeaders: GbGradeTable.headerRenderer,
    columns: [{
      renderer: GbGradeTable.courseGradeRenderer,
      editor: false,
    }].concat(GbGradeTable.columns.map(function (column) {
      if (column.type === 'category') {
        return {
          renderer: GbGradeTable.cellRenderer,
          editor: false
        };
      } else {
        return {
          renderer: GbGradeTable.cellRenderer,
          editor: GbGradeTableEditor
        };
      }
    })),
    colWidths: [140].concat(GbGradeTable.columns.map(function () { return 230 })),
    autoRowSize: false,
    autoColSize: false,
    height: $(window).height() * 0.4,
    width: $('#' + elementId).width() * 0.9,
    fillHandle: false,
    afterGetRowHeader: function(row,th) {
      $(th).
        attr("role", "rowheader").
        attr("scope", "row");
    },
    afterGetColHeader: function(col, th) {
      var $th = $(th);
      $th.
        attr("role", "columnheader").
        attr("scope", "col").
        addClass("gb-categorized"); /* TODO only if enabled */

      // student column
      if (col == -1) {
        $th.html(GbGradeTable.templates.studentHeader.process());

      // assignment column
      } else if (col > 0) {
        var column = GbGradeTable.columns[col - 1];
        var name = column.title;
        $th.
          attr("role", "columnheader").
          attr("scope", "col").
          attr("abbr", name).
          attr("aria-label", name).
          css("borderTopColor", column.color || column.categoryColor);
        
        $th.find(".swatch").css("backgroundColor", column.color || column.categoryColor);
      }
    },
    beforeOnCellMouseDown: function(event, coords, td) {
      if (coords.row < 0 && coords.col >= 0) {
        event.stopImmediatePropagation();
        this.selectCell(0, coords.col);
      } else if (coords.col < 0) {
        event.stopImmediatePropagation();
        this.selectCell(coords.row, 0);
      }
    },
    currentRowClassName: 'currentRow',
    currentColClassName: 'currentCol',
    multiSelect: false,
  });

  // append all dropdown menus to body to avoid overflows on table
  var $dropdownMenu;
  var $link;
  $(window).on('show.bs.dropdown', function (event) {
    $dropdownMenu = $(event.target).find('.dropdown-menu');
    $link = $(event.target);

    $dropdownMenu.width($dropdownMenu.outerWidth());

    $('body').append($dropdownMenu.detach());

    var linkOffset = $link.offset();

    $dropdownMenu.css({
        'display': 'block',
        'top': linkOffset.top + $link.outerHeight(),
        'left': linkOffset.left - $dropdownMenu.outerWidth() + $link.outerWidth()
    });
  });
  $(window).on('hide.bs.dropdown', function (event) {
    $link.append($dropdownMenu.detach());
    $dropdownMenu.hide();
    $dropdownMenu = null;
  });
  $(".wtHolder").on('scroll', function (event) {
    if ($dropdownMenu && $dropdownMenu.length > 0) {
      var linkOffset = $link.offset();

      $dropdownMenu.css({
          'top': linkOffset.top + $link.outerHeight(),
          'left': linkOffset.left - $dropdownMenu.outerWidth() + $link.outerWidth()
      });
    }
  });

};
