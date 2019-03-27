$(function () {
    $tooltips = $('a[data-toggle="tooltip"]', 'div.comment');
    $tooltips
        .click(function (e) {
            e.preventDefault();
            $tooltips.tooltip('hide');
        })
        .tooltip();
});
