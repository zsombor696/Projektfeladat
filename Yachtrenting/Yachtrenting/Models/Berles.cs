using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

public class Berles
{
    public int Id { get; set; }

    [Required]
    public int Uid { get; set; }

    [Required]
    public int YachtId { get; set; }

    [Required]
    public DateTime StartDate { get; set; }

    [Required]
    public DateTime EndDate { get; set; }

    [Required]
    public int DailyPrice { get; set; }

    [Required]
    public int Deposit { get; set; }

    [NotMapped]
    public int TotalPrice => (EndDate - StartDate).Days * DailyPrice;
}
