$(function(){
    $("form").submit(check_data);
    $("input").focus(clear_error);
});


function check_data() {
    var newPassword = $("#new-password").val();
    var confirmPassword = $("#confirm-password").val();
    if(newPassword != confirmPassword) {
        $("#confirm-password").addClass("is-invalid");
        return false;
    }
    return true;
}

function clear_error() {
    $(this).removeClass("is-invalid");
}
