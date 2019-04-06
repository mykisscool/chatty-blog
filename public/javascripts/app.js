$(function () {

    // Comment author tooltips
    $tooltips = $('a[data-toggle="tooltip"]', 'div.comment');
    $tooltips
        .click(function (event) {
            event.preventDefault();
            $tooltips.tooltip('hide');
            foldComments(event.target);
        })
        .tooltip();

    // Comment folding
    $('div.comment', 'div#comments').prev('p').find('a > i').show();

    // Login form validation
    if (document.getElementById('form-login')) {

        window.addEventListener('load', function () {
            var form = document.getElementById('form-login');
            form.addEventListener('submit', function (e) {

                // Invalid submission
                if (form.checkValidity() === false) {
                    e.preventDefault();
                    e.stopPropagation();
                }

                // Valid submission
                else {
                    $('button', form).prop('disabled', true);
                }

                form.classList.add('was-validated');

            }, false);
        }, false);
    }
});

/** Folds/unfolds children comments based on parent comment link location
 *
 * @param link The link with the +/- that was clicked/tapped
 */
function foldComments(link) {
    $('i', link).toggleClass('folded');
    $(link).parent('p').next('div.comment').toggleClass('d-none');
}
