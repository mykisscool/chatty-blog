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

    // Toggle reply box
    $('span.reply', 'div.comment').click(function (e) {
        e.preventDefault();
        $parent = $(this).parents('p');
        $textareaInput = '' +
            '<div class="input-group">' +
                '<textarea class="form-control rounded-0 reply-to" rows="2" aria-label="Reply"></textarea>' +
                '<div class="input-group-append">' +
                    '<button class="btn btn-outline-primary" type="button">Reply</button>' +
                '</div>' +
            '</div>';

        if ($parent.find('div.input-group').length) {
            $parent.find('div.input-group').remove();
        }
        else {
            $parent.append($textareaInput);
        }
    });

    // Reply box response
    $('div.comment').on('click', 'button', function (e) {
        e.stopPropagation();
        $parent = $(this).parents('div.input-group');

        var replyData = $parent.prev('span.reply').data(),
            postid = replyData.postid,
            commentid = replyData.commentid,
            comment = $parent.find('textarea.reply-to').val(),
            csrfToken = $(':hidden[name=csrfToken]', 'section.article-body article').val();

        if (comment.trim().length) {
            var route = jsRoutes.controllers.BlogPostController.addComment(postid, commentid, comment);

            // @TODO update DOM, etc.
            $.ajax({
                method: 'post',
                url: route.url,
                contentType : 'application/json',
                beforeSend: function(xhr) {
                    xhr.setRequestHeader('Csrf-Token', csrfToken);
                }
            });
        }
    });
});

/** Folds/unfolds children comments based on parent comment link location
 *
 * @param link The link with the +/- that was clicked/tapped
 */
function foldComments(link) {
    $('i', link).toggleClass('folded');
    $(link).parent('p').next('div.comment').toggleClass('d-none');
}
